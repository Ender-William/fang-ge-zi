package com.pigeonnest.presentation.pigeonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.usecase.pigeon.DeletePigeonUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import com.pigeonnest.domain.usecase.pigeon.SearchPigeonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PigeonListUiState {
    data object Loading : PigeonListUiState()
    data class Success(val pigeons: List<Pigeon>) : PigeonListUiState()
    data object Empty : PigeonListUiState()
    data class Error(val message: String) : PigeonListUiState()
}

@HiltViewModel
class PigeonListViewModel @Inject constructor(
    private val getPigeonListUseCase: GetPigeonListUseCase,
    private val searchPigeonsUseCase: SearchPigeonsUseCase,
    private val deletePigeonUseCase: DeletePigeonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PigeonListUiState>(PigeonListUiState.Loading)
    val uiState: StateFlow<PigeonListUiState> = _uiState

    private val _recentPigeons = MutableStateFlow<List<Pigeon>>(emptyList())
    val recentPigeons: StateFlow<List<Pigeon>> = _recentPigeons

    private var currentQuery = ""

    init {
        loadPigeons()
        loadRecentPigeons()
    }

    private fun loadPigeons() {
        viewModelScope.launch {
            if (currentQuery.isBlank()) {
                getPigeonListUseCase().collectLatest { pigeons ->
                    _uiState.value = if (pigeons.isEmpty()) {
                        PigeonListUiState.Empty
                    } else {
                        PigeonListUiState.Success(pigeons)
                    }
                }
            } else {
                searchPigeonsUseCase(currentQuery).collectLatest { pigeons ->
                    _uiState.value = if (pigeons.isEmpty()) {
                        PigeonListUiState.Empty
                    } else {
                        PigeonListUiState.Success(pigeons)
                    }
                }
            }
        }
    }

    private fun loadRecentPigeons() {
        viewModelScope.launch {
            try {
                // GetPigeonListUseCase returns Flow, we collect latest
                getPigeonListUseCase().collectLatest { pigeons ->
                    _recentPigeons.value = pigeons.take(10)
                }
            } catch (_: Exception) {
                _recentPigeons.value = emptyList()
            }
        }
    }

    fun search(query: String) {
        currentQuery = query
        loadPigeons()
    }

    fun deletePigeon(pigeonId: String) {
        viewModelScope.launch {
            deletePigeonUseCase(pigeonId)
        }
    }
}
