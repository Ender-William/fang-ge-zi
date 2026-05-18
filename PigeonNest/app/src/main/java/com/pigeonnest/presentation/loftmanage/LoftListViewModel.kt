package com.pigeonnest.presentation.loftmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.usecase.loft.DeleteLoftUseCase
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.loft.SaveLoftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoftListViewModel @Inject constructor(
    private val getLoftListUseCase: GetLoftListUseCase,
    private val saveLoftUseCase: SaveLoftUseCase,
    private val deleteLoftUseCase: DeleteLoftUseCase
) : ViewModel() {

    private val _lofts = MutableStateFlow<List<Loft>>(emptyList())
    val lofts: StateFlow<List<Loft>> = _lofts

    init {
        viewModelScope.launch {
            getLoftListUseCase().collectLatest {
                _lofts.value = it
            }
        }
    }

    fun addLoft(name: String) {
        viewModelScope.launch {
            saveLoftUseCase(name = name)
        }
    }

    fun updateLoft(loft: Loft) {
        viewModelScope.launch {
            saveLoftUseCase(
                id = loft.id,
                name = loft.name,
                location = loft.location,
                description = loft.description,
                capacity = loft.capacity
            )
        }
    }

    fun deleteLoft(loftId: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = deleteLoftUseCase(loftId)
            result.fold(
                onSuccess = { callback(true, "删除成功") },
                onFailure = { callback(false, it.message ?: "删除失败") }
            )
        }
    }
}
