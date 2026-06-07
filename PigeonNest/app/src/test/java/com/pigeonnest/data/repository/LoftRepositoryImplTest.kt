package com.pigeonnest.data.repository

import app.cash.turbine.test
import com.pigeonnest.data.local.dao.LoftDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.data.local.mapper.LoftMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoftRepositoryImplTest {

    private val loftDao: LoftDao = mockk()
    private val pigeonDao: PigeonDao = mockk()
    private val loftMapper = LoftMapper()
    private val repository = LoftRepositoryImpl(loftDao, pigeonDao, loftMapper)

    @Test
    fun `getAllLofts maps entities with pigeonCount`() = runTest {
        val loftEntity = LoftEntity("l1", "测试鸽舍", createdAt = 1L, updatedAt = 1L)
        every { loftDao.getAllFlow() } returns flowOf(listOf(loftEntity))
        coEvery { pigeonDao.getCountByLoft("l1") } returns 5

        repository.getAllLofts().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("测试鸽舍", list[0].name)
            assertEquals(5, list[0].pigeonCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLoftById returns mapped loft with count`() = runTest {
        val loftEntity = LoftEntity("l1", "测试鸽舍", createdAt = 1L, updatedAt = 1L)
        coEvery { loftDao.getById("l1") } returns loftEntity
        coEvery { pigeonDao.getCountByLoft("l1") } returns 3

        val result = repository.getLoftById("l1")

        assertNotNull(result)
        assertEquals(3, result!!.pigeonCount)
    }

    @Test
    fun `getLoftById returns null when not found`() = runTest {
        coEvery { loftDao.getById("unknown") } returns null

        val result = repository.getLoftById("unknown")

        assertEquals(null, result)
    }

    @Test
    fun `saveLoft inserts entity and returns id`() = runTest {
        coEvery { loftDao.insert(any()) } returns Unit

        val loft = com.pigeonnest.domain.model.Loft(id = "l1", name = "新鸽舍")
        val result = repository.saveLoft(loft)

        assertTrue(result.isSuccess)
        assertEquals("l1", result.getOrNull())
    }

    @Test
    fun `deleteLoft fails when pigeons remain`() = runTest {
        coEvery { pigeonDao.getCountByLoft("l1") } returns 3

        val result = repository.deleteLoft("l1")

        assertTrue(result.isFailure)
        assertEquals("鸽舍中还有鸽子，请先移走", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteLoft succeeds when empty`() = runTest {
        coEvery { pigeonDao.getCountByLoft("l1") } returns 0
        coEvery { loftDao.softDelete("l1") } returns Unit

        val result = repository.deleteLoft("l1")

        assertTrue(result.isSuccess)
        coVerify { loftDao.softDelete("l1") }
    }

    @Test
    fun `updateSortOrder updates all lofts`() = runTest {
        coEvery { loftDao.updateSortOrder(any(), any()) } returns Unit

        val result = repository.updateSortOrder(listOf("l3", "l1", "l2"))

        assertTrue(result.isSuccess)
        coVerify { loftDao.updateSortOrder("l3", 0) }
        coVerify { loftDao.updateSortOrder("l1", 1) }
        coVerify { loftDao.updateSortOrder("l2", 2) }
    }
}
