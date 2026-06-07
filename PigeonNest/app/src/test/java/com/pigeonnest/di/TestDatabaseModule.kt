package com.pigeonnest.di

import android.content.Context
import androidx.room.Room
import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.LocationHistoryDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.database.PigeonNestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PigeonNestDatabase {
        return Room.inMemoryDatabaseBuilder(
            context.applicationContext,
            PigeonNestDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun providePigeonDao(database: PigeonNestDatabase): PigeonDao = database.pigeonDao()

    @Provides
    fun provideLoftDao(database: PigeonNestDatabase): LoftDao = database.loftDao()

    @Provides
    fun provideLocationHistoryDao(database: PigeonNestDatabase): LocationHistoryDao =
        database.locationHistoryDao()

    @Provides
    fun provideFamilyRelationDao(database: PigeonNestDatabase): FamilyRelationDao =
        database.familyRelationDao()

    @Provides
    fun providePigeonPhotoDao(database: PigeonNestDatabase): PigeonPhotoDao =
        database.pigeonPhotoDao()
}
