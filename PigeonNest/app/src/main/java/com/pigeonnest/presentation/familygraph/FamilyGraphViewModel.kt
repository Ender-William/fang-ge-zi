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

    private val _currentDepth = MutableStateFlow(10)
    val currentDepth: StateFlow<Int> = _currentDepth

    private var currentPigeonId: String? = null

    companion object {
        const val MIN_DEPTH = 1
        const val MAX_DEPTH = 20
    }

    fun loadGraph(pigeonId: String) {
        currentPigeonId = pigeonId
        reload()
    }

    fun setDepth(depth: Int) {
        val clamped = depth.coerceIn(MIN_DEPTH, MAX_DEPTH)
        if (_currentDepth.value != clamped) {
            _currentDepth.value = clamped
            reload()
        }
    }

    fun increaseDepth() {
        setDepth(_currentDepth.value + 1)
    }

    fun decreaseDepth() {
        setDepth(_currentDepth.value - 1)
    }

    private fun reload() {
        val pigeonId = currentPigeonId ?: return
        viewModelScope.launch {
            try {
                val pigeon = pigeonRepository.getPigeonById(pigeonId).firstOrNull()

                if (pigeon == null) {
                    _graphData.value = Result.failure(Exception("鸽子不存在"))
                    return@launch
                }

                val layoutResult = GraphLayoutManager.buildGraphFromPigeon(
                    pigeon,
                    familyRepository,
                    _currentDepth.value
                )
                _graphData.value = Result.success(layoutResult)
            } catch (e: Exception) {
                _graphData.value = Result.failure(e)
            }
        }
    }
}
