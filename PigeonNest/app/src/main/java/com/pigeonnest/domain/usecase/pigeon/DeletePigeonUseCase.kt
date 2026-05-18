package com.pigeonnest.domain.usecase.pigeon

import com.pigeonnest.domain.repository.PigeonRepository
import javax.inject.Inject

class DeletePigeonUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository
) {
    suspend operator fun invoke(pigeonId: String): Result<Unit> {
        return pigeonRepository.deletePigeon(pigeonId)
    }
}
