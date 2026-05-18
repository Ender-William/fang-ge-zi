package com.pigeonnest.presentation.locationset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.SavePigeonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSetViewModel @Inject constructor(
    private val getLoftListUseCase: GetLoftListUseCase,
    private val getPigeonDetailUseCase: GetPigeonDetailUseCase,
    private val savePigeonUseCase: SavePigeonUseCase
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

    fun updateLocation(pigeonId: String, loftId: String?, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val pigeon = getPigeonDetailUseCase(pigeonId).let { flow ->
                    var p: com.pigeonnest.domain.model.Pigeon? = null
                    flow.collect { p = it }
                    p
                }

                if (pigeon == null) {
                    callback(false, "鸽子不存在")
                    return@launch
                }

                val result = savePigeonUseCase(
                    SavePigeonUseCase.Params(
                        id = pigeonId,
                        name = pigeon.name,
                        ringNumber = pigeon.ringNumber,
                        color = pigeon.color,
                        gender = pigeon.gender,
                        birthDate = pigeon.birthDate,
                        entryDate = pigeon.entryDate,
                        loftId = loftId,
                        cageNumber = pigeon.cageNumber,
                        status = pigeon.status,
                        notes = pigeon.notes
                    )
                )

                result.fold(
                    onSuccess = { callback(true, "位置变更成功") },
                    onFailure = { callback(false, it.message ?: "变更失败") }
                )
            } catch (e: Exception) {
                callback(false, e.message ?: "变更失败")
            }
        }
    }
}
