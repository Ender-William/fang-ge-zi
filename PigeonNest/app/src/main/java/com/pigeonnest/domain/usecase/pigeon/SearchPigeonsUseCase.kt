package com.pigeonnest.domain.usecase.pigeon

import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.repository.PigeonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPigeonsUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository
) {
    operator fun invoke(query: String): Flow<List<Pigeon>> {
        return if (query.isBlank()) {
            pigeonRepository.getAllPigeons()
        } else {
            pigeonRepository.searchPigeons(query.trim())
        }
    }
}
