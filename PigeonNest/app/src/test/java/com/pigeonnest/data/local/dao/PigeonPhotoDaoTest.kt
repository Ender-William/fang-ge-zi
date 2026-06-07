package com.pigeonnest.data.local.dao

import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.data.local.entity.PigeonPhotoEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PigeonPhotoDaoTest : DaoTestBase() {

    @Test
    fun `insert and retrieve photos by pigeon ordered by created_at DESC`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        val photo1 = PigeonPhotoEntity(pigeonId = "p1", photoPath = "/p1/1.jpg", createdAt = 1000L)
        val photo2 = PigeonPhotoEntity(pigeonId = "p1", photoPath = "/p1/2.jpg", createdAt = 2000L)
        pigeonPhotoDao.insert(photo1)
        pigeonPhotoDao.insert(photo2)

        val photos = pigeonPhotoDao.getByPigeonId("p1")
        assertEquals(2, photos.size)
        assertEquals("/p1/2.jpg", photos[0].photoPath)
    }

    @Test
    fun `getPrimaryPhoto returns primary photo`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        val primary = PigeonPhotoEntity(pigeonId = "p1", photoPath = "/p1/primary.jpg", isPrimary = true)
        val normal = PigeonPhotoEntity(pigeonId = "p1", photoPath = "/p1/normal.jpg", isPrimary = false)
        pigeonPhotoDao.insert(primary)
        pigeonPhotoDao.insert(normal)

        val result = pigeonPhotoDao.getPrimaryPhoto("p1")
        assertNotNull(result)
        assertEquals("/p1/primary.jpg", result!!.photoPath)
    }

    @Test
    fun `deleteByPigeonId removes all photos for pigeon`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        pigeonPhotoDao.insert(PigeonPhotoEntity(pigeonId = "p1", photoPath = "/p1/1.jpg"))
        pigeonPhotoDao.insert(PigeonPhotoEntity(pigeonId = "p1", photoPath = "/p1/2.jpg"))

        pigeonPhotoDao.deleteByPigeonId("p1")

        assertEquals(0, pigeonPhotoDao.getByPigeonId("p1").size)
    }

    @Test
    fun `deleteById removes single photo`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insert(pigeon)

        val photo = PigeonPhotoEntity(id = "photo-1", pigeonId = "p1", photoPath = "/p1/1.jpg")
        pigeonPhotoDao.insert(photo)

        pigeonPhotoDao.deleteById("photo-1")

        assertEquals(0, pigeonPhotoDao.getByPigeonId("p1").size)
    }
}
