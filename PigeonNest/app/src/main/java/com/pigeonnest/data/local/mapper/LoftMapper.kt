package com.pigeonnest.data.local.mapper

import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.domain.model.Loft
import javax.inject.Inject

class LoftMapper @Inject constructor() {

    fun toDomain(entity: LoftEntity, pigeonCount: Int = 0): Loft {
        return Loft(
            id = entity.id,
            name = entity.name,
            location = entity.location,
            description = entity.description,
            capacity = entity.capacity,
            colorTag = entity.colorTag,
            sortOrder = entity.sortOrder,
            pigeonCount = pigeonCount
        )
    }

    fun toEntity(domain: Loft): LoftEntity {
        return LoftEntity(
            id = domain.id,
            name = domain.name,
            location = domain.location,
            description = domain.description,
            capacity = domain.capacity,
            colorTag = domain.colorTag,
            sortOrder = domain.sortOrder
        )
    }
}
