package com.pigeonnest.data.local.dao

import app.cash.turbine.test
import com.pigeonnest.data.local.entity.LoftEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LoftDaoTest : DaoTestBase() {

    @Test
    fun `insert and retrieve loft`() = runTest {
        val loft = LoftEntity("l1", "测试鸽舍", createdAt = 1000L, updatedAt = 1000L)
        loftDao.insert(loft)

        val result = loftDao.getById("l1")
        assertNotNull(result)
        assertEquals("测试鸽舍", result!!.name)
    }

    @Test
    fun `getAllFlow excludes soft deleted and sorts by sort_order then created_at`() = runTest {
        val loft1 = LoftEntity("l1", "B", sortOrder = 2, createdAt = 1L, updatedAt = 1L)
        val loft2 = LoftEntity("l2", "A", sortOrder = 1, createdAt = 2L, updatedAt = 2L)
        val deleted = LoftEntity("l3", "D", sortOrder = 0, createdAt = 0L, updatedAt = 0L, isDeleted = 1)
        loftDao.insertAll(listOf(loft1, loft2, deleted))

        loftDao.getAllFlow().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("A", list[0].name)
            assertEquals("B", list[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `softDelete marks loft as deleted`() = runTest {
        val loft = LoftEntity("l1", "测试鸽舍", createdAt = 1L, updatedAt = 1L)
        loftDao.insert(loft)

        loftDao.softDelete("l1")

        assertNull(loftDao.getById("l1"))
    }

    @Test
    fun `updateSortOrder changes order`() = runTest {
        val loft = LoftEntity("l1", "测试鸽舍", sortOrder = 0, createdAt = 1L, updatedAt = 1L)
        loftDao.insert(loft)

        loftDao.updateSortOrder("l1", 5)

        assertEquals(5, loftDao.getById("l1")!!.sortOrder)
    }

    @Test
    fun `update modifies loft`() = runTest {
        val loft = LoftEntity("l1", "旧名称", createdAt = 1L, updatedAt = 1L)
        loftDao.insert(loft)

        loftDao.update(loft.copy(name = "新名称"))

        assertEquals("新名称", loftDao.getById("l1")!!.name)
    }
}
