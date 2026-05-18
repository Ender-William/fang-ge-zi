package com.pigeonnest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pigeonnest.data.local.entity.PigeonPhotoEntity

@Dao
interface PigeonPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PigeonPhotoEntity)

    @Query("SELECT * FROM pigeon_photos WHERE pigeon_id = :pigeonId ORDER BY created_at DESC")
    suspend fun getByPigeonId(pigeonId: String): List<PigeonPhotoEntity>

    @Query("SELECT * FROM pigeon_photos WHERE pigeon_id = :pigeonId AND is_primary = 1 LIMIT 1")
    suspend fun getPrimaryPhoto(pigeonId: String): PigeonPhotoEntity?

    @Query("DELETE FROM pigeon_photos WHERE id = :photoId")
    suspend fun deleteById(photoId: String)

    @Query("DELETE FROM pigeon_photos WHERE pigeon_id = :pigeonId")
    suspend fun deleteByPigeonId(pigeonId: String)

    @Query("DELETE FROM pigeon_photos")
    suspend fun deleteAll()

    @Query("SELECT * FROM pigeon_photos")
    suspend fun getAll(): List<PigeonPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PigeonPhotoEntity>)
}
