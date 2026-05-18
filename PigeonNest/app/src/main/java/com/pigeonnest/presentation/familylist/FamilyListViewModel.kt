package com.pigeonnest.presentation.familylist

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.FamilyGroup
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FamilyListViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val getPigeonListUseCase: GetPigeonListUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("family_names", Context.MODE_PRIVATE)

    private val _families = MutableStateFlow<List<FamilyGroup>>(emptyList())
    val families: StateFlow<List<FamilyGroup>> = _families

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFamilies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allPigeons = getPigeonListUseCase().first()
                if (allPigeons.isEmpty()) {
                    _families.value = emptyList()
                    return@launch
                }

                val pigeonMap = allPigeons.associateBy { it.id }

                // 在 IO 线程执行家族分组，避免主线程卡顿
                val result = withContext(Dispatchers.IO) {
                    // 批量获取所有家族关系，避免 N+1 查询
                    val allRelations = mutableMapOf<String, com.pigeonnest.domain.model.FamilyRelation>()
                    for (pigeon in allPigeons) {
                        try {
                            familyRepository.getFamilyRelation(pigeon.id)?.let {
                                allRelations[pigeon.id] = it
                            }
                        } catch (_: Exception) { }
                    }

                    // 找出没有父亲也没有母亲的鸽子（家族根）
                    val rootIds = allPigeons.map { it.id }.filter { pigeonId ->
                        val relation = allRelations[pigeonId]
                        relation == null || (relation.father == null && relation.mother == null)
                    }

                    val processedIds = mutableSetOf<String>()
                    val groups = mutableListOf<FamilyGroup>()

                    for (rootId in rootIds) {
                        if (rootId in processedIds) continue

                        // 获取家族树，depth=5 足够统计数量
                        val graphData = familyRepository.getGraphData(rootId, depth = 5)
                        val familyPigeonIds = graphData.allNodes.map { it.pigeonId }.toSet()
                        processedIds.addAll(familyPigeonIds)

                        val familyPigeons = familyPigeonIds.mapNotNull { pigeonMap[it] }

                        val rootPigeon = pigeonMap[rootId]
                        val matePigeon = rootPigeon?.let { root ->
                            allRelations[root.id]?.mate?.let { mateBrief ->
                                pigeonMap[mateBrief.id]
                            }
                        }

                        val customName = prefs.getString("family_name_$rootId", null)

                        groups.add(
                            FamilyGroup(
                                rootPigeonId = rootId,
                                rootPigeonName = rootPigeon?.name ?: "",
                                matePigeonName = matePigeon?.name,
                                rootColor = rootPigeon?.color,
                                mateColor = matePigeon?.color,
                                totalCount = familyPigeons.size,
                                maleCount = familyPigeons.count { it.gender == Gender.MALE },
                                femaleCount = familyPigeons.count { it.gender == Gender.FEMALE },
                                customName = customName
                            )
                        )
                    }

                    groups.sortedByDescending { it.totalCount }
                }

                _families.value = result
            } catch (e: Exception) {
                _families.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setCustomFamilyName(rootPigeonId: String, name: String?) {
        prefs.edit().apply {
            if (name.isNullOrBlank()) {
                remove("family_name_$rootPigeonId")
            } else {
                putString("family_name_$rootPigeonId", name.trim())
            }
            apply()
        }
        loadFamilies()
    }
}
