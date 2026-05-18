package com.pigeonnest.domain.usecase.loft

import com.pigeonnest.domain.repository.LoftRepository
import javax.inject.Inject

class DeleteLoftUseCase @Inject constructor(
    private val loftRepository: LoftRepository
) {
    suspend operator fun invoke(loftId: String): Result<Unit> {
        return loftRepository.deleteLoft(loftId)
    }
}
