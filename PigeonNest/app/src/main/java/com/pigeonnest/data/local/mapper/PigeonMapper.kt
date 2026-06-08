package com.pigeonnest.data.local.mapper

import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonStatus
import javax.inject.Inject

class PigeonMapper @Inject constructor() {

    fun toDomain(entity: PigeonEntity, loft: Loft? = null): Pigeon {
        return Pigeon(
            id = entity.id,
            ringNumber = entity.ringNumber,
            name = entity.name,
            color = entity.color,
            gender = Gender.fromCode(entity.gender),
            birthDate = entity.birthDate,
            entryDate = entity.entryDate,
            photoPath = entity.photoPath,
            eyePhotoPath = entity.eyePhotoPath,
            loft = loft,
            cageNumber = entity.cageNumber,
            status = PigeonStatus.fromCode(entity.status),
            notes = entity.notes,
            achievement = entity.achievement,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Pigeon): PigeonEntity {
        return PigeonEntity(
            id = domain.id,
            ringNumber = domain.ringNumber,
            name = domain.name,
            color = domain.color,
            gender = domain.gender.code,
            birthDate = domain.birthDate,
            entryDate = domain.entryDate,
            photoPath = domain.photoPath,
            eyePhotoPath = domain.eyePhotoPath,
            loftId = domain.loft?.id,
            cageNumber = domain.cageNumber,
            status = domain.status.code,
            notes = domain.notes,
            achievement = domain.achievement,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
