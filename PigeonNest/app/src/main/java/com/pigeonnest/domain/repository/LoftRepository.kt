package com.pigeonnest.domain.repository

import com.pigeonnest.domain.model.Loft
import kotlinx.coroutines.flow.Flow

interface LoftRepository {
    fun getAllLofts(): Flow<List<Loft>>
    suspend fun getLoftById(loftId: String): Loft?
    suspend fun saveLoft(loft: Loft): Result<String>
    suspend fun deleteLoft(loftId: String): Result<Unit>
    suspend fun updateSortOrder(loftIds: List<String>): Result<Unit>
}
