package com.pigeonnest.data.repository

import android.net.Uri
import app.cash.turbine.test
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.LocationHistoryDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.dao.PigeonPhotoDao
import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.data.local.mapper.PigeonMapper
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.Pigeon
import com.pigeonnest.domain.model.PigeonStatus
import com.pigeonnest.domain.repository.LoftRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PigeonRepositoryImplTest {

    private val pigeonDao: PigeonDao = mockk()
    private val pigeonPhotoDao: PigeonPhotoDao = mockk()
    private val locationHistoryDao: LocationHistoryDao = mockk()
    private val familyRelationDao: FamilyRelationDao = mockk()
    private val photoStorage: PhotoStorageManager = mockk()
    private val pigeonMapper = PigeonMapper()
    private val loftRepository: LoftRepository = mockk()
    private val repository = PigeonRepositoryImpl(
        pigeonDao, pigeonPhotoDao, locationHistoryDao,
        familyRelationDao, photoStorage, pigeonMapper, loftRepository
    )

    @Test
    fun `getAllPigeons maps entities with loft`() = runTest {
        val entity = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, loftId = "l1", createdAt = 1L, updatedAt = 1L)
        every { pigeonDao.getAllPigeonsFlow() } returns flowOf(listOf(entity))
        coEvery { loftRepository.getLoftById("l1") } returns Loft("l1", "测试鸽舍")

        repository.getAllPigeons().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("小白", list[0].name)
            assertEquals("测试鸽舍", list[0].loft?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `savePigeon inserts new pigeon`() = runTest {
        coEvery { pigeonDao.getById("p1") } returns null
        coEvery { pigeonDao.insert(any()) } returns 1L

        val pigeon = Pigeon("p1", "R1", "小白", gender = Gender.MALE, status = PigeonStatus.ACTIVE)
        val result = repository.savePigeon(pigeon)

        assertTrue(result.isSuccess)
        assertEquals("p1", result.getOrNull())
        coVerify { pigeonDao.insert(any()) }
    }

    @Test
    fun `savePigeon updates existing pigeon`() = runTest {
        val existing = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        coEvery { pigeonDao.getById("p1") } returns existing
        coEvery { pigeonDao.update(any()) } returns Unit

        val pigeon = Pigeon("p1", "R1", "小白-更新", gender = Gender.MALE, status = PigeonStatus.ACTIVE)
        val result = repository.savePigeon(pigeon)

        assertTrue(result.isSuccess)
        coVerify { pigeonDao.update(any()) }
        coVerify(exactly = 0) { pigeonDao.insert(any()) }
    }

    @Test
    fun `deletePigeon clears family references and soft deletes`() = runTest {
        coEvery { familyRelationDao.clearFatherReference("p1") } returns Unit
        coEvery { familyRelationDao.clearMotherReference("p1") } returns Unit
        coEvery { familyRelationDao.clearMateReference("p1") } returns Unit
        coEvery { familyRelationDao.deleteByPigeonId("p1") } returns Unit
        coEvery { photoStorage.deletePigeonPhotos("p1") } returns true
        coEvery { pigeonDao.softDelete("p1") } returns Unit

        val result = repository.deletePigeon("p1")

        assertTrue(result.isSuccess)
        coVerify { familyRelationDao.clearFatherReference("p1") }
        coVerify { familyRelationDao.clearMotherReference("p1") }
        coVerify { familyRelationDao.clearMateReference("p1") }
        coVerify { familyRelationDao.deleteByPigeonId("p1") }
        coVerify { photoStorage.deletePigeonPhotos("p1") }
        coVerify { pigeonDao.softDelete("p1") }
    }

    @Test
    fun `updatePigeonLocation records history when loft changes`() = runTest {
        val existing = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, loftId = "l1", createdAt = 1L, updatedAt = 1L)
        coEvery { pigeonDao.getById("p1") } returns existing
        coEvery { locationHistoryDao.insert(any()) } returns Unit
        coEvery { pigeonDao.update(any()) } returns Unit

        val result = repository.updatePigeonLocation("p1", "l2", "B-02")

        assertTrue(result.isSuccess)
        coVerify { locationHistoryDao.insert(any()) }
    }

    @Test
    fun `updatePigeonLocation does not record history when loft unchanged`() = runTest {
        val existing = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, loftId = "l1", createdAt = 1L, updatedAt = 1L)
        coEvery { pigeonDao.getById("p1") } returns existing
        coEvery { pigeonDao.update(any()) } returns Unit

        val result = repository.updatePigeonLocation("p1", "l1", "A-01")

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { locationHistoryDao.insert(any()) }
    }

    @Test
    fun `addPigeonPhoto saves photo and inserts record`() = runTest {
        val uri = mockk<Uri>()
        coEvery { photoStorage.savePigeonPhoto("p1", uri) } returns "/photos/p1.jpg"
        coEvery { pigeonPhotoDao.insert(any()) } returns Unit

        val result = repository.addPigeonPhoto("p1", uri, "测试照片")

        assertTrue(result.isSuccess)
        assertEquals("/photos/p1.jpg", result.getOrNull())
        coVerify { pigeonPhotoDao.insert(any()) }
    }

    @Test
    fun `deletePigeonPhoto deletes by id`() = runTest {
        coEvery { pigeonPhotoDao.deleteById("photo-1") } returns Unit

        val result = repository.deletePigeonPhoto("photo-1")

        assertTrue(result.isSuccess)
        coVerify { pigeonPhotoDao.deleteById("photo-1") }
    }
}
