package com.pigeonnest.presentation.familylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.FamilyGroup
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyListViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val getPigeonListUseCase: GetPigeonListUseCase
) : ViewModel() {

    private val _families = MutableStateFlow<List<FamilyGroup>>(emptyList())
    val families: StateFlow<List<FamilyGroup>> = _families

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFamilies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allPigeons = getPigeonListUseCase().first()
                val pigeonMap = allPigeons.associateBy { it.id }

                // 找出没有父亲也没有母亲的鸽子（家族根）
                val rootIds = allPigeons.map { it.id }.filter { pigeonId ->
                    val relation = familyRepository.getFamilyRelation(pigeonId)
                    relation == null || (relation.father == null && relation.mother == null)
                }

                val processedIds = mutableSetOf<String>()
                val result = mutableListOf<FamilyGroup>()

                for (rootId in rootIds) {
                    if (rootId in processedIds) continue

                    // 获取整个家族树
                    val graphData = familyRepository.getGraphData(rootId, depth = 10)
                    val familyPigeonIds = graphData.allNodes.map { it.pigeonId }.toSet()
                    processedIds.addAll(familyPigeonIds)

                    val familyPigeons = familyPigeonIds.mapNotNull { pigeonMap[it] }

                    val rootPigeon = pigeonMap[rootId]
                    val matePigeon = rootPigeon?.let { root ->
                        val relation = familyRepository.getFamilyRelation(root.id)
                        relation?.mate?.let { mateBrief ->
                            pigeonMap[mateBrief.id]
                        }
                    }

                    result.add(
                        FamilyGroup(
                            rootPigeonId = rootId,
                            rootPigeonName = rootPigeon?.name ?: "",
                            matePigeonName = matePigeon?.name,
                            rootColor = rootPigeon?.color,
                            mateColor = matePigeon?.color,
                            totalCount = familyPigeons.size,
                            maleCount = familyPigeons.count { it.gender == Gender.MALE },
                            femaleCount = familyPigeons.count { it.gender == Gender.FEMALE }
                        )
                    )
                }

                _families.value = result.sortedByDescending { it.totalCount }
            } catch (e: Exception) {
                _families.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
