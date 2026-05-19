package com.pigeonnest.presentation.pigeondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.domain.model.FamilyRelation
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.usecase.family.UpdateFamilyRelationUseCase
import com.pigeonnest.domain.usecase.pigeon.DeletePigeonUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PigeonDetailViewModel @Inject constructor(
    private val getPigeonDetailUseCase: GetPigeonDetailUseCase,
    private val deletePigeonUseCase: DeletePigeonUseCase,
    private val familyRepository: FamilyRepository,
    private val updateFamilyRelationUseCase: UpdateFamilyRelationUseCase,
    private val getPigeonListUseCase: GetPigeonListUseCase
) : ViewModel() {

    private val _pigeon = MutableStateFlow<Pigeon?>(null)
    val pigeon: StateFlow<Pigeon?> = _pigeon

    private val _familyRelation = MutableStateFlow<FamilyRelation?>(null)
    val familyRelation: StateFlow<FamilyRelation?> = _familyRelation

    private val _allPigeons = MutableStateFlow<List<Pigeon>>(emptyList())
    val allPigeons: StateFlow<List<Pigeon>> = _allPigeons

    private val _updateResult = MutableStateFlow<Result<Unit>?>(null)
    val updateResult: StateFlow<Result<Unit>?> = _updateResult

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    fun loadPigeon(pigeonId: String) {
        viewModelScope.launch {
            getPigeonDetailUseCase(pigeonId).collectLatest {
                _pigeon.value = it
            }
        }
        loadFamilyRelation(pigeonId)
        loadAllPigeons()
    }

    private fun loadFamilyRelation(pigeonId: String) {
        viewModelScope.launch {
            try {
                _familyRelation.value = familyRepository.getFamilyRelation(pigeonId)
            } catch (_: Exception) {
                _familyRelation.value = null
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

    fun updateFamilyRelation(
        pigeonId: String,
        fatherId: String?,
        motherId: String?,
        mateId: String?
    ) {
        viewModelScope.launch {
            val result = updateFamilyRelationUseCase(
                pigeonId = pigeonId,
                fatherId = fatherId,
                motherId = motherId,
                mateId = mateId
            )
            _updateResult.value = result
            if (result.isSuccess) {
                loadFamilyRelation(pigeonId)
            }
        }
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun deletePigeon(pigeonId: String) {
        viewModelScope.launch {
            val result = deletePigeonUseCase(pigeonId)
            _deleteResult.value = result
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}
