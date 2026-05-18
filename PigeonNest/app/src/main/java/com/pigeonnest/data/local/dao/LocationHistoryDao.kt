package com.pigeonnest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pigeonnest.data.local.entity.LocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: LocationHistoryEntity)

    @Query("SELECT * FROM location_history WHERE pigeon_id = :pigeonId ORDER BY move_date DESC")
    fun getByPigeonId(pigeonId: String): Flow<List<LocationHistoryEntity>>

    @Query("DELETE FROM location_history WHERE pigeon_id = :pigeonId")
    suspend fun deleteByPigeonId(pigeonId: String)

    @Query("SELECT * FROM location_history ORDER BY move_date DESC")
    suspend fun getAll(): List<LocationHistoryEntity>

    @Query("DELETE FROM location_history")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyList: List<LocationHistoryEntity>)
}
