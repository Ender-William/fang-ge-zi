package com.pigeonnest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pigeonnest.data.local.entity.LoftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loft: LoftEntity)

    @Update
    suspend fun update(loft: LoftEntity)

    @Delete
    suspend fun delete(loft: LoftEntity)

    @Query("DELETE FROM lofts WHERE id = :loftId")
    suspend fun deleteById(loftId: String)

    @Query("SELECT * FROM lofts WHERE is_deleted = 0 ORDER BY sort_order, created_at")
    fun getAllFlow(): Flow<List<LoftEntity>>

    @Query("SELECT * FROM lofts WHERE is_deleted = 0 ORDER BY sort_order, created_at")
    suspend fun getAll(): List<LoftEntity>

    @Query("SELECT * FROM lofts WHERE id = :loftId AND is_deleted = 0")
    suspend fun getById(loftId: String): LoftEntity?

    @Query("UPDATE lofts SET sort_order = :order WHERE id = :loftId")
    suspend fun updateSortOrder(loftId: String, order: Int)

    @Query("UPDATE lofts SET is_deleted = 1 WHERE id = :loftId")
    suspend fun softDelete(loftId: String)

    @Query("DELETE FROM lofts")
    suspend fun deleteAll()
}
