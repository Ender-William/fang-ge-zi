package com.pigeonnest.presentation.pigeonedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import com.pigeonnest.domain.usecase.pigeon.SavePigeonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PigeonEditViewModel @Inject constructor(
    private val getPigeonDetailUseCase: GetPigeonDetailUseCase,
    private val savePigeonUseCase: SavePigeonUseCase,
    private val getLoftListUseCase: GetLoftListUseCase,
    private val getPigeonListUseCase: GetPigeonListUseCase
) : ViewModel() {

    private val _pigeon = MutableStateFlow<Pigeon?>(null)
    val pigeon: StateFlow<Pigeon?> = _pigeon

    private val _saveResult = MutableStateFlow<Result<String>?>(null)
    val saveResult: StateFlow<Result<String>?> = _saveResult

    private val _lofts = MutableStateFlow<List<Loft>>(emptyList())
    val lofts: StateFlow<List<Loft>> = _lofts

    private val _allPigeons = MutableStateFlow<List<Pigeon>>(emptyList())
    val allPigeons: StateFlow<List<Pigeon>> = _allPigeons

    private val _selectedGender = MutableStateFlow(Gender.UNKNOWN)
    private val _birthDate = MutableStateFlow<Long?>(null)
    private val _selectedColor = MutableStateFlow<String?>(null)
    private val _photoUri = MutableStateFlow<Uri?>(null)
    private val _eyePhotoUri = MutableStateFlow<Uri?>(null)
    private val _loftId = MutableStateFlow<String?>(null)
    private val _cageNumber = MutableStateFlow<String?>(null)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep

    // 家族关系
    private val _fatherId = MutableStateFlow<String?>(null)
    val fatherId: StateFlow<String?> = _fatherId
    private val _motherId = MutableStateFlow<String?>(null)
    val motherId: StateFlow<String?> = _motherId
    private val _mateId = MutableStateFlow<String?>(null)
    val mateId: StateFlow<String?> = _mateId

    init {
        loadLofts()
        loadAllPigeons()
    }

    private fun loadLofts() {
        viewModelScope.launch {
            getLoftListUseCase().collectLatest {
                _lofts.value = it
            }
        }
    }

    private fun loadAllPigeons() {
        viewModelScope.launch {
            getPigeonListUseCase().collectLatest {
                _allPigeons.value = it
            }
        }
    }

    fun loadPigeon(pigeonId: String) {
        viewModelScope.launch {
            getPigeonDetailUseCase(pigeonId).collectLatest {
                _pigeon.value = it
            }
        }
    }

    fun onPigeonLoaded(pigeon: Pigeon) {
        _selectedGender.value = pigeon.gender
        _birthDate.value = pigeon.birthDate
        _selectedColor.value = pigeon.color
        _loftId.value = pigeon.loft?.id
        _cageNumber.value = pigeon.cageNumber
        pigeon.familyRelation?.let { relation ->
            _fatherId.value = relation.father?.id
            _motherId.value = relation.mother?.id
            _mateId.value = relation.mate?.id
        }
    }

    fun setGender(gender: Gender) {
        _selectedGender.value = gender
    }

    fun setBirthDate(date: Long?) {
        _birthDate.value = date
    }

    fun setColor(color: String?) {
        _selectedColor.value = color
    }

    fun setPhotoUri(uri: Uri?) {
        _photoUri.value = uri
    }

    fun setEyePhotoUri(uri: Uri?) {
        _eyePhotoUri.value = uri
    }

    fun setLoftId(loftId: String?) {
        _loftId.value = loftId
    }

    fun setCageNumber(cage: String?) {
        _cageNumber.value = cage
    }

    fun setFatherId(id: String?) {
        _fatherId.value = id
    }

    fun setMotherId(id: String?) {
        _motherId.value = id
    }

    fun setMateId(id: String?) {
        _mateId.value = id
    }

    fun setStep(step: Int) {
        _currentStep.value = step.coerceIn(1, 3)
    }

    fun nextStep() {
        _currentStep.value = (_currentStep.value + 1).coerceAtMost(3)
    }

    fun prevStep() {
        _currentStep.value = (_currentStep.value - 1).coerceAtLeast(1)
    }

    fun save(
        id: String?,
        name: String,
        ringNumber: String,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val result = savePigeonUseCase(
                    SavePigeonUseCase.Params(
                        id = id,
                        name = name,
                        ringNumber = ringNumber,
                        color = _selectedColor.value,
                        gender = _selectedGender.value,
                        birthDate = _birthDate.value,
                        loftId = _loftId.value,
                        cageNumber = _cageNumber.value,
                        notes = notes,
                        photoUri = _photoUri.value,
                        photoPath = _pigeon.value?.photoPath,
                        eyePhotoUri = _eyePhotoUri.value,
                        eyePhotoPath = _pigeon.value?.eyePhotoPath,
                        fatherId = _fatherId.value,
                        motherId = _motherId.value,
                        mateId = _mateId.value
                    )
                )
                _saveResult.value = result
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun clearResult() {
        _saveResult.value = null
    }
}
