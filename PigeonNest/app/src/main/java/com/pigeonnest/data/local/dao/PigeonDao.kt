package com.pigeonnest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pigeonnest.data.local.entity.PigeonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PigeonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pigeon: PigeonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pigeons: List<PigeonEntity>)

    @Update
    suspend fun update(pigeon: PigeonEntity)

    @Delete
    suspend fun delete(pigeon: PigeonEntity)

    @Query("DELETE FROM pigeons WHERE id = :pigeonId")
    suspend fun deleteById(pigeonId: String)

    @Query("UPDATE pigeons SET is_deleted = 1 WHERE id = :pigeonId")
    suspend fun softDelete(pigeonId: String)

    @Query("DELETE FROM pigeons")
    suspend fun deleteAll()

    @Query("SELECT * FROM pigeons WHERE is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllPigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE is_deleted = 0 ORDER BY updated_at DESC")
    suspend fun getAll(): List<PigeonEntity>

    @Query("SELECT * FROM pigeons WHERE id = :pigeonId AND is_deleted = 0")
    suspend fun getById(pigeonId: String): PigeonEntity?

    @Query("SELECT * FROM pigeons WHERE id = :pigeonId AND is_deleted = 0")
    fun getByIdFlow(pigeonId: String): Flow<PigeonEntity?>

    @Query("""
        SELECT * FROM pigeons 
        WHERE is_deleted = 0 
        AND (name LIKE '%' || :query || '%' 
           OR ring_number LIKE '%' || :query || '%'
           OR color LIKE '%' || :query || '%'
           OR cage_number LIKE '%' || :query || '%')
        ORDER BY updated_at DESC
    """)
    fun searchPigeons(query: String): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE status = :status AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getByStatus(status: Int): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE loft_id = :loftId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getByLoft(loftId: String): Flow<List<PigeonEntity>>

    @Query("SELECT COUNT(*) FROM pigeons WHERE status = 0 AND is_deleted = 0")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM pigeons WHERE loft_id = :loftId AND is_deleted = 0")
    suspend fun getCountByLoft(loftId: String): Int

    @Query("SELECT * FROM pigeons WHERE gender = :gender AND is_deleted = 0 ORDER BY name")
    fun getByGender(gender: Int): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE is_deleted = 0 ORDER BY updated_at DESC LIMIT 10")
    suspend fun getRecentPigeons(): List<PigeonEntity>
}
