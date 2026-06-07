package com.pigeonnest.data.local.dao

import app.cash.turbine.test
import com.pigeonnest.data.local.entity.LocationHistoryEntity
import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.data.local.entity.PigeonEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationHistoryDaoTest : DaoTestBase() {

    @Test
    fun `insert and retrieve location history ordered by moveDate DESC`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)
        val loft1 = LoftEntity("l1", "鸽舍1", createdAt = 1L, updatedAt = 1L)
        val loft2 = LoftEntity("l2", "鸽舍2", createdAt = 1L, updatedAt = 1L)
        val loft3 = LoftEntity("l3", "鸽舍3", createdAt = 1L, updatedAt = 1L)
        loftDao.insertAll(listOf(loft1, loft2, loft3))

        val history1 = LocationHistoryEntity(pigeonId = "p1", fromLoftId = "l1", toLoftId = "l2", moveDate = 1000L)
        val history2 = LocationHistoryEntity(pigeonId = "p1", fromLoftId = "l2", toLoftId = "l3", moveDate = 2000L)
        locationHistoryDao.insert(history1)
        locationHistoryDao.insert(history2)

        locationHistoryDao.getByPigeonId("p1").test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("l2", list[0].fromLoftId)
            assertEquals("l1", list[1].fromLoftId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteByPigeonId removes all history for pigeon`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        locationHistoryDao.insert(LocationHistoryEntity(pigeonId = "p1", moveDate = 1000L))
        locationHistoryDao.insert(LocationHistoryEntity(pigeonId = "p1", moveDate = 2000L))

        locationHistoryDao.deleteByPigeonId("p1")

        locationHistoryDao.getByPigeonId("p1").test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
