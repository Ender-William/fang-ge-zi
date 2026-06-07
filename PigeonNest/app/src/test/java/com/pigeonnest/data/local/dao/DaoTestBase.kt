package com.pigeonnest.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pigeonnest.data.local.database.PigeonNestDatabase
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class DaoTestBase {

    protected lateinit var database: PigeonNestDatabase
    protected lateinit var pigeonDao: PigeonDao
    protected lateinit var loftDao: LoftDao
    protected lateinit var familyRelationDao: FamilyRelationDao
    protected lateinit var locationHistoryDao: LocationHistoryDao
    protected lateinit var pigeonPhotoDao: PigeonPhotoDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            PigeonNestDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        pigeonDao = database.pigeonDao()
        loftDao = database.loftDao()
        familyRelationDao = database.familyRelationDao()
        locationHistoryDao = database.locationHistoryDao()
        pigeonPhotoDao = database.pigeonPhotoDao()
    }

    @After
    fun teardownDatabase() {
        database.close()
    }
}
