package com.pigeonnest.domain.usecase.family

import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LineageResult
import javax.inject.Inject

class GetLineageUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(pigeonId: String, generations: Int = 3): LineageResult {
        return familyRepository.getLineage(pigeonId, generations)
    }
}
