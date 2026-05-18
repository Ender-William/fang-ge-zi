package com.pigeonnest.domain.usecase.loft

import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.repository.LoftRepository
import java.util.UUID
import javax.inject.Inject

class SaveLoftUseCase @Inject constructor(
    private val loftRepository: LoftRepository
) {
    suspend operator fun invoke(
        id: String? = null,
        name: String,
        location: String? = null,
        description: String? = null,
        capacity: Int? = null,
        colorTag: String? = null
    ): Result<String> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("鸽舍名称不能为空"))
        }

        val loft = Loft(
            id = id ?: UUID.randomUUID().toString(),
            name = name.trim(),
            location = location?.trim(),
            description = description?.trim(),
            capacity = capacity,
            colorTag = colorTag
        )

        return loftRepository.saveLoft(loft)
    }
}
