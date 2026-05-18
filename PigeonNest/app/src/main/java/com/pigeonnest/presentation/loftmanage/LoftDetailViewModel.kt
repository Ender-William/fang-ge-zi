package com.pigeonnest.presentation.loftmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoftDetailViewModel @Inject constructor(
    private val loftRepository: LoftRepository,
    private val getPigeonListUseCase: GetPigeonListUseCase
) : ViewModel() {

    private val _loft = MutableStateFlow<Loft?>(null)
    val loft: StateFlow<Loft?> = _loft

    private val _pigeons = MutableStateFlow<List<Pigeon>>(emptyList())
    val pigeons: StateFlow<List<Pigeon>> = _pigeons

    fun loadLoft(loftId: String) {
        viewModelScope.launch {
            _loft.value = loftRepository.getLoftById(loftId)
        }
        viewModelScope.launch {
            getPigeonListUseCase(loftId = loftId).collectLatest {
                _pigeons.value = it
            }
        }
    }
}
