package com.pigeonnest.data.local.dao

import app.cash.turbine.test
import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.data.local.entity.PigeonEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PigeonDaoTest : DaoTestBase() {

    @Test
    fun `insert and retrieve pigeon`() = runTest {
        val pigeon = PigeonEntity(
            id = "p1",
            ringNumber = "2024-001",
            name = "小白",
            gender = 1,
            status = 0,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        pigeonDao.insert(pigeon)

        val result = pigeonDao.getById("p1")
        assertNotNull(result)
        assertEquals("小白", result!!.name)
    }

    @Test
    fun `getAllPigeonsFlow excludes soft deleted`() = runTest {
        val active = PigeonEntity("p1", "R1", "A", status = 0, createdAt = 1L, updatedAt = 3L)
        val deleted = PigeonEntity("p2", "R2", "D", status = 0, createdAt = 2L, updatedAt = 2L, isDeleted = 1)
        pigeonDao.insertAll(listOf(active, deleted))

        pigeonDao.getAllPigeonsFlow().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("A", list[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPigeons searches multiple fields`() = runTest {
        val pigeon = PigeonEntity(
            id = "p1", ringNumber = "2024-001", name = "小白",
            color = "白色", cageNumber = "A-01",
            gender = 1, status = 0, createdAt = 1L, updatedAt = 1L
        )
        pigeonDao.insert(pigeon)

        // Search by name
        pigeonDao.searchPigeons("小白").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // Search by ring number
        pigeonDao.searchPigeons("2024").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // Search by color
        pigeonDao.searchPigeons("白色").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // Search by cage number
        pigeonDao.searchPigeons("A-01").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // No match
        pigeonDao.searchPigeons("不存在").test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByStatus filters correctly`() = runTest {
        val active = PigeonEntity("p1", "R1", "A", status = 0, createdAt = 1L, updatedAt = 1L)
        val sold = PigeonEntity("p2", "R2", "S", status = 1, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(active, sold))

        pigeonDao.getByStatus(0).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByLoft filters correctly`() = runTest {
        val loft = LoftEntity("loft-1", "测试鸽舍", createdAt = 1L, updatedAt = 1L)
        loftDao.insert(loft)
        val inLoft = PigeonEntity("p1", "R1", "A", loftId = "loft-1", status = 0, createdAt = 1L, updatedAt = 1L)
        val noLoft = PigeonEntity("p2", "R2", "B", status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(inLoft, noLoft))

        pigeonDao.getByLoft("loft-1").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByGender filters correctly`() = runTest {
        val male = PigeonEntity("p1", "R1", "M", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val female = PigeonEntity("p2", "R2", "F", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(male, female))

        pigeonDao.getByGender(1).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `softDelete marks pigeon as deleted`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        pigeonDao.softDelete("p1")

        assertNull(pigeonDao.getById("p1"))
        pigeonDao.getAllPigeonsFlow().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActiveCount returns correct count`() = runTest {
        val active = PigeonEntity("p1", "R1", "A", status = 0, createdAt = 1L, updatedAt = 1L)
        val sold = PigeonEntity("p2", "R2", "S", status = 1, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(active, sold))

        assertEquals(1, pigeonDao.getActiveCount())
    }

    @Test
    fun `getCountByLoft returns correct count`() = runTest {
        val loft1 = LoftEntity("loft-1", "鸽舍1", createdAt = 1L, updatedAt = 1L)
        val loft2 = LoftEntity("loft-2", "鸽舍2", createdAt = 1L, updatedAt = 1L)
        loftDao.insertAll(listOf(loft1, loft2))
        val inLoft1 = PigeonEntity("p1", "R1", "A", loftId = "loft-1", status = 0, createdAt = 1L, updatedAt = 1L)
        val inLoft2 = PigeonEntity("p2", "R2", "B", loftId = "loft-2", status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(inLoft1, inLoft2))

        assertEquals(1, pigeonDao.getCountByLoft("loft-1"))
        assertEquals(1, pigeonDao.getCountByLoft("loft-2"))
    }

    @Test
    fun `getRecentPigeons returns last 10 ordered by updated_at DESC`() = runTest {
        val pigeons = (1..12).map {
            PigeonEntity("p$it", "R$it", "P$it", status = 0, createdAt = it.toLong(), updatedAt = it.toLong())
        }
        pigeonDao.insertAll(pigeons)

        val recent = pigeonDao.getRecentPigeons()
        assertEquals(10, recent.size)
        assertEquals("P12", recent[0].name)
        assertEquals("P3", recent[9].name)
    }

    @Test
    fun `update modifies existing pigeon`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        pigeonDao.update(pigeon.copy(name = "Updated"))

        assertEquals("Updated", pigeonDao.getById("p1")!!.name)
    }
}
