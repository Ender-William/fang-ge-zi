package com.pigeonnest.data.repository

import android.net.Uri
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.data.local.dao.LocationHistoryDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.entity.LocationHistoryEntity
import com.pigeonnest.data.local.entity.PigeonPhotoEntity
import com.pigeonnest.data.local.mapper.PigeonMapper
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.repository.PigeonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PigeonRepositoryImpl @Inject constructor(
    private val pigeonDao: PigeonDao,
    private val pigeonPhotoDao: PigeonPhotoDao,
    private val locationHistoryDao: LocationHistoryDao,
    private val photoStorage: PhotoStorageManager,
    private val pigeonMapper: PigeonMapper,
    private val loftRepository: LoftRepository
) : PigeonRepository {

    private suspend fun entityToPigeon(entity: com.pigeonnest.data.local.entity.PigeonEntity): Pigeon {
        val loft = entity.loftId?.let { loftRepository.getLoftById(it) }
        return pigeonMapper.toDomain(entity, loft)
    }

    override fun getAllPigeons(): Flow<List<Pigeon>> {
        return pigeonDao.getAllPigeonsFlow()
            .flatMapLatest { entities ->
                flow {
                    emit(entities.map { entityToPigeon(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPigeonById(pigeonId: String): Flow<Pigeon?> {
        return pigeonDao.getByIdFlow(pigeonId)
            .flatMapLatest { entity ->
                flow {
                    emit(entity?.let { entityToPigeon(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun searchPigeons(query: String): Flow<List<Pigeon>> {
        return pigeonDao.searchPigeons(query)
            .flatMapLatest { entities ->
                flow {
                    emit(entities.map { entityToPigeon(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPigeonsByLoft(loftId: String): Flow<List<Pigeon>> {
        return pigeonDao.getByLoft(loftId)
            .flatMapLatest { entities ->
                flow {
                    emit(entities.map { entityToPigeon(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPigeonsByStatus(status: PigeonStatus): Flow<List<Pigeon>> {
        return pigeonDao.getByStatus(status.code)
            .flatMapLatest { entities ->
                flow {
                    emit(entities.map { entityToPigeon(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPigeonsByGender(genderCode: Int): Flow<List<Pigeon>> {
        return pigeonDao.getByGender(genderCode)
            .flatMapLatest { entities ->
                flow {
                    emit(entities.map { entityToPigeon(it) })
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getActivePigeonCount(): Int {
        return pigeonDao.getActiveCount()
    }

    override suspend fun getPigeonCountByLoft(loftId: String): Int {
        return pigeonDao.getCountByLoft(loftId)
    }

    override suspend fun getRecentPigeons(): List<Pigeon> {
        return pigeonDao.getRecentPigeons().map { entityToPigeon(it) }
    }

    override suspend fun savePigeon(pigeon: Pigeon): Result<String> {
        return try {
            val entity = pigeonMapper.toEntity(pigeon).copy(updatedAt = System.currentTimeMillis())
            val existing = pigeonDao.getById(pigeon.id)
            if (existing != null) {
                // 编辑已有鸽子：使用 update，避免 REPLACE 触发外键级联删除
                pigeonDao.update(entity)
            } else {
                pigeonDao.insert(entity)
            }
            Result.success(pigeon.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePigeon(pigeonId: String): Result<Unit> {
        return try {
            photoStorage.deletePigeonPhotos(pigeonId)
            pigeonDao.softDelete(pigeonId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePigeonLocation(
        pigeonId: String,
        loftId: String?,
        cageNumber: String?
    ): Result<Unit> {
        return try {
            val entity = pigeonDao.getById(pigeonId)
                ?: return Result.failure(Exception("鸽子不存在"))

            val fromLoftId = entity.loftId
            if (fromLoftId != loftId) {
                locationHistoryDao.insert(
                    LocationHistoryEntity(
                        pigeonId = pigeonId,
                        fromLoftId = fromLoftId,
                        toLoftId = loftId,
                        moveDate = System.currentTimeMillis()
                    )
                )
            }

            pigeonDao.update(
                entity.copy(
                    loftId = loftId,
                    cageNumber = cageNumber,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPigeonPhoto(pigeonId: String, uri: Uri, caption: String?): Result<String> {
        return try {
            val photoPath = photoStorage.savePigeonPhoto(pigeonId, uri)
            val photoEntity = PigeonPhotoEntity(
                pigeonId = pigeonId,
                photoPath = photoPath,
                caption = caption,
                takenDate = System.currentTimeMillis(),
                isPrimary = false
            )
            pigeonPhotoDao.insert(photoEntity)
            Result.success(photoPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePigeonPhoto(photoId: String): Result<Unit> {
        return try {
            pigeonPhotoDao.deleteById(photoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
