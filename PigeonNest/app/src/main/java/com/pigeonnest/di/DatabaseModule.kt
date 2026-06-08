package com.pigeonnest.di

import android.content.Context
import androidx.room.Room
import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.LocationHistoryDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.database.MIGRATION_1_2
import com.pigeonnest.data.local.database.MIGRATION_2_3
import com.pigeonnest.data.local.database.MIGRATION_3_4
import com.pigeonnest.data.local.database.PigeonNestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PigeonNestDatabase {
        val passphrase = net.sqlcipher.database.SQLiteDatabase.getBytes("pigeon_nest_secure".toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context.applicationContext,
            PigeonNestDatabase::class.java,
            PigeonNestDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
