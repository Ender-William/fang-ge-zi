package com.pigeonnest.domain.usecase.loft

import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.repository.LoftRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoftListUseCase @Inject constructor(
    private val loftRepository: LoftRepository
) {
    operator fun invoke(): Flow<List<Loft>> {
        return loftRepository.getAllLofts()
    }
}
