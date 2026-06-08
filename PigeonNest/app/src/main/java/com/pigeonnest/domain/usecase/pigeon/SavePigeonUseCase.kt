package com.pigeonnest.domain.usecase.pigeon

import android.net.Uri
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.repository.PigeonRepository
import com.pigeonnest.domain.usecase.family.UpdateFamilyRelationUseCase
import java.util.UUID
import javax.inject.Inject

class SavePigeonUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository,
    private val loftRepository: LoftRepository,
    private val updateFamilyRelationUseCase: UpdateFamilyRelationUseCase,
    private val photoStorage: PhotoStorageManager
) {
    data class Params(
        val id: String? = null,
        val ringNumber: String,
        val name: String,
        val color: String? = null,
        val gender: Gender = Gender.UNKNOWN,
        val birthDate: Long? = null,
        val entryDate: Long? = null,
        val loftId: String? = null,
        val cageNumber: String? = null,
        val status: PigeonStatus = PigeonStatus.ACTIVE,
        val notes: String? = null,
        val achievement: String? = null,
        val photoUri: Uri? = null,
        val photoPath: String? = null,
        val eyePhotoUri: Uri? = null,
        val eyePhotoPath: String? = null,
        val fatherId: String? = null,
        val motherId: String? = null,
        val mateId: String? = null
    )

    suspend operator fun invoke(params: Params): Result<String> {
        val validationError = validateParams(params)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val loft = params.loftId?.let { loftRepository.getLoftById(it) }

        // 统一生成鸽子 ID，避免照片目录与鸽子记录 ID 不一致
        val pigeonId = params.id ?: UUID.randomUUID().toString()

        // 处理照片：如果有新照片则保存并获取路径，否则保留原路径
        val photoPath = if (params.photoUri != null) {
            photoStorage.savePigeonPhoto(
                pigeonId = pigeonId,
                sourceUri = params.photoUri
            )
        } else {
            params.photoPath
        }

        // 处理眼睛照片
        val eyePhotoPath = if (params.eyePhotoUri != null) {
            photoStorage.savePigeonPhoto(
                pigeonId = pigeonId,
                sourceUri = params.eyePhotoUri
            )
        } else {
            params.eyePhotoPath
        }

        val pigeon = Pigeon(
            id = pigeonId,
            ringNumber = params.ringNumber.trim(),
            name = params.name.trim(),
            color = params.color?.trim(),
            gender = params.gender,
            birthDate = params.birthDate,
            entryDate = params.entryDate ?: System.currentTimeMillis(),
            photoPath = photoPath,
            eyePhotoPath = eyePhotoPath,
            loft = loft,
            cageNumber = params.cageNumber?.trim(),
            status = params.status,
            notes = params.notes?.trim(),
            achievement = params.achievement?.trim()
        )

        val saveResult = pigeonRepository.savePigeon(pigeon)

        if (saveResult.isSuccess) {
            // 保存家族关系
            updateFamilyRelationUseCase(
                pigeonId = pigeon.id,
                fatherId = params.fatherId,
                motherId = params.motherId,
                mateId = params.mateId
            )
        }

        return saveResult
    }

    private fun validateParams(params: Params): String? {
        if (params.ringNumber.isBlank()) return "足环号不能为空"
        if (params.name.isBlank()) return "鸽子名称不能为空"
        if (params.ringNumber.length > 50) return "足环号过长（最多50字符）"
        if (params.name.length > 30) return "名称过长（最多30字符）"
        return null
    }
}
