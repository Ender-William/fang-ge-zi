package com.pigeonnest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pigeonnest.data.local.entity.FamilyRelationEntity

@Dao
interface FamilyRelationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: FamilyRelationEntity)

    @Update
    suspend fun update(relation: FamilyRelationEntity)

    @Query("SELECT * FROM family_relations WHERE pigeon_id = :pigeonId")
    suspend fun getByPigeonId(pigeonId: String): FamilyRelationEntity?

    @Query("SELECT * FROM family_relations WHERE father_id = :pigeonId OR mother_id = :pigeonId")
    suspend fun getChildrenRelations(pigeonId: String): List<FamilyRelationEntity>

    @Query("SELECT * FROM family_relations WHERE mate_id = :pigeonId")
    suspend fun getMateRelations(pigeonId: String): List<FamilyRelationEntity>

    @Query("DELETE FROM family_relations WHERE pigeon_id = :pigeonId")
    suspend fun deleteByPigeonId(pigeonId: String)

    @Query("DELETE FROM family_relations")
    suspend fun deleteAll()

    @Query("SELECT * FROM family_relations")
    suspend fun getAll(): List<FamilyRelationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<FamilyRelationEntity>)
}
