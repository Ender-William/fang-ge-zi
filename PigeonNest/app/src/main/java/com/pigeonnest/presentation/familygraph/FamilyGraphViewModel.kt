package com.pigeonnest.presentation.familygraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.LayoutResult
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.PigeonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyGraphViewModel @Inject constructor(
    private val pigeonRepository: PigeonRepository,
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _graphData = MutableStateFlow<Result<LayoutResult>?>(null)
    val graphData: StateFlow<Result<LayoutResult>?> = _graphData

    fun loadGraph(pigeonId: String) {
        viewModelScope.launch {
            try {
                val pigeon = pigeonRepository.getPigeonById(pigeonId).firstOrNull()

                if (pigeon == null) {
                    _graphData.value = Result.failure(Exception("鸽子不存在"))
                    return@launch
                }

                val layoutResult = GraphLayoutManager.buildGraphFromPigeon(
                    pigeon,
                    familyRepository
                )
                _graphData.value = Result.success(layoutResult)
            } catch (e: Exception) {
                _graphData.value = Result.failure(e)
            }
        }
    }
}
