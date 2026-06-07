package com.pigeonnest.presentation.pigeondetail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pigeonnest.data.file.PigeonPdfGenerator
import com.pigeonnest.domain.model.FamilyRelation
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.usecase.family.GetLineageUseCase
import com.pigeonnest.domain.usecase.family.UpdateFamilyRelationUseCase
import com.pigeonnest.domain.usecase.pigeon.DeletePigeonUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PigeonDetailViewModel @Inject constructor(
    private val getPigeonDetailUseCase: GetPigeonDetailUseCase,
    private val deletePigeonUseCase: DeletePigeonUseCase,
    private val familyRepository: FamilyRepository,
    private val updateFamilyRelationUseCase: UpdateFamilyRelationUseCase,
    private val getPigeonListUseCase: GetPigeonListUseCase,
    private val getLineageUseCase: GetLineageUseCase,
    private val loftRepository: LoftRepository,
    private val pigeonPdfGenerator: PigeonPdfGenerator
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

    private val _pdfPreviewFile = MutableStateFlow<File?>(null)
    val pdfPreviewFile: StateFlow<File?> = _pdfPreviewFile

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf

    private val _pdfError = MutableStateFlow<String?>(null)
    val pdfError: StateFlow<String?> = _pdfError

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

    fun generateExportPdf(pigeonId: String) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            _pdfError.value = null
            try {
                val currentPigeon = getPigeonDetailUseCase(pigeonId).first()
                    ?: throw IllegalStateException("鸽子信息不存在")
                val lineage = getLineageUseCase(pigeonId, generations = 3)
                val loft: Loft? = currentPigeon.loft?.id?.let {
                    loftRepository.getLoftById(it)
                }
                val file = pigeonPdfGenerator.generate(currentPigeon, lineage, loft)
                _pdfPreviewFile.value = file
            } catch (e: Exception) {
                _pdfError.value = e.message ?: "生成 PDF 失败"
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun clearPdfPreviewFile() {
        _pdfPreviewFile.value = null
    }

    fun clearPdfError() {
        _pdfError.value = null
    }

    fun getPdfShareUri(file: File): Uri {
        return pigeonPdfGenerator.getFileUri(file)
    }
}
