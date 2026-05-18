package com.pigeonnest.domain.usecase.family

import com.pigeonnest.domain.repository.FamilyRepository
import javax.inject.Inject

class UpdateFamilyRelationUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(
        pigeonId: String,
        fatherId: String? = null,
        motherId: String? = null,
        mateId: String? = null
    ): Result<Unit> {
        val parentResult = if (fatherId != null || motherId != null) {
            familyRepository.updateParents(pigeonId, fatherId, motherId)
        } else {
            Result.success(Unit)
        }

        val mateResult = if (mateId != null) {
            familyRepository.updateMate(pigeonId, mateId)
        } else {
            Result.success(Unit)
        }

        return if (parentResult.isFailure) {
            parentResult
        } else if (mateResult.isFailure) {
            mateResult
        } else {
            Result.success(Unit)
        }
    }
}
