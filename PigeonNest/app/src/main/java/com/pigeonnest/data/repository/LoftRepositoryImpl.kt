package com.pigeonnest.data.repository

import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.mapper.LoftMapper
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.repository.LoftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoftRepositoryImpl @Inject constructor(
    private val loftDao: LoftDao,
    private val pigeonDao: PigeonDao,
    private val loftMapper: LoftMapper
) : LoftRepository {

    override fun getAllLofts(): Flow<List<Loft>> {
        return loftDao.getAllFlow()
            .map { list ->
                list.map { entity ->
                    val count = pigeonDao.getCountByLoft(entity.id)
                    loftMapper.toDomain(entity, count)
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getLoftById(loftId: String): Loft? {
        val entity = loftDao.getById(loftId) ?: return null
        val count = pigeonDao.getCountByLoft(loftId)
        return loftMapper.toDomain(entity, count)
    }

    override suspend fun saveLoft(loft: Loft): Result<String> {
        return try {
            val entity = loftMapper.toEntity(loft)
            loftDao.insert(entity)
            Result.success(loft.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLoft(loftId: String): Result<Unit> {
        return try {
            val count = pigeonDao.getCountByLoft(loftId)
            if (count > 0) {
                return Result.failure(Exception("鸽舍中还有鸽子，请先移走"))
            }
            loftDao.softDelete(loftId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSortOrder(loftIds: List<String>): Result<Unit> {
        return try {
            loftIds.forEachIndexed { index, id ->
                loftDao.updateSortOrder(id, index)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
