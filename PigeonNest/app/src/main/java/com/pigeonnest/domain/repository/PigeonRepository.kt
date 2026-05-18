package com.pigeonnest.domain.repository

import android.net.Uri
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonStatus
import kotlinx.coroutines.flow.Flow

interface PigeonRepository {
    fun getAllPigeons(): Flow<List<Pigeon>>
    fun getPigeonById(pigeonId: String): Flow<Pigeon?>
    fun searchPigeons(query: String): Flow<List<Pigeon>>
    fun getPigeonsByLoft(loftId: String): Flow<List<Pigeon>>
    fun getPigeonsByStatus(status: PigeonStatus): Flow<List<Pigeon>>
    fun getPigeonsByGender(genderCode: Int): Flow<List<Pigeon>>

    suspend fun getActivePigeonCount(): Int
    suspend fun getPigeonCountByLoft(loftId: String): Int
    suspend fun getRecentPigeons(): List<Pigeon>

    suspend fun savePigeon(pigeon: Pigeon): Result<String>
    suspend fun deletePigeon(pigeonId: String): Result<Unit>
    suspend fun updatePigeonLocation(pigeonId: String, loftId: String?, cageNumber: String?): Result<Unit>

    suspend fun addPigeonPhoto(pigeonId: String, uri: Uri, caption: String? = null): Result<String>
    suspend fun deletePigeonPhoto(photoId: String): Result<Unit>
}
