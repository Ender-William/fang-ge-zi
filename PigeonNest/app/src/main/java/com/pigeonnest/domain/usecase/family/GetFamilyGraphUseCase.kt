package com.pigeonnest.domain.usecase.family

import com.pigeonnest.domain.model.GraphData
import com.pigeonnest.domain.repository.FamilyRepository
import javax.inject.Inject

class GetFamilyGraphUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(pigeonId: String, depth: Int = 3): GraphData {
        return familyRepository.getGraphData(pigeonId, depth)
    }
}
