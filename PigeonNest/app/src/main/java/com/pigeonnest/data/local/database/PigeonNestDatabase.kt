package com.pigeonnest.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.LocationHistoryDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.entity.FamilyRelationEntity
import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.data.local.entity.LocationHistoryEntity
import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.data.local.entity.PigeonPhotoEntity

@Database(
    entities = [
        PigeonEntity::class,
        LoftEntity::class,
        LocationHistoryEntity::class,
        FamilyRelationEntity::class,
        PigeonPhotoEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class PigeonNestDatabase : RoomDatabase() {
    abstract fun pigeonDao(): PigeonDao
    abstract fun loftDao(): LoftDao
    abstract fun locationHistoryDao(): LocationHistoryDao
    abstract fun familyRelationDao(): FamilyRelationDao
    abstract fun pigeonPhotoDao(): PigeonPhotoDao

    companion object {
        const val DATABASE_NAME = "pigeon_nest.db"
    }
}
