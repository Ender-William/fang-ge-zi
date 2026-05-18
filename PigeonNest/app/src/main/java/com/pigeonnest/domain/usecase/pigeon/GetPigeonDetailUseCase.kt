package com.pigeonnest.domain.usecase.pigeon

import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.PigeonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPigeonDetailUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository,
    private val familyRepository: FamilyRepository
) {
    operator fun invoke(pigeonId: String): Flow<Pigeon?> {
        return pigeonRepository.getPigeonById(pigeonId).map { pigeon ->
            pigeon?.copy(
                familyRelation = familyRepository.getFamilyRelation(pigeonId)
            )
        }
    }
}
