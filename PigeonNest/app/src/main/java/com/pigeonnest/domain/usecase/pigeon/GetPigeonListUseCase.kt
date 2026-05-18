package com.pigeonnest.domain.usecase.pigeon

import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.repository.PigeonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPigeonListUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository
) {
    operator fun invoke(
        loftId: String? = null,
        status: PigeonStatus? = null
    ): Flow<List<Pigeon>> {
        return when {
            loftId != null -> pigeonRepository.getPigeonsByLoft(loftId)
            status != null -> pigeonRepository.getPigeonsByStatus(status)
            else -> pigeonRepository.getAllPigeons()
        }
    }
}
