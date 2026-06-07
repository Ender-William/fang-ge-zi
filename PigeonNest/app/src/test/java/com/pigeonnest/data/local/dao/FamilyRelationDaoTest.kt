package com.pigeonnest.data.local.dao

import com.pigeonnest.data.local.entity.FamilyRelationEntity
import com.pigeonnest.data.local.entity.PigeonEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FamilyRelationDaoTest : DaoTestBase() {

    @Test
    fun `insert and retrieve family relation`() = runTest {
        // Need to insert pigeons first due to FK constraints
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val father = PigeonEntity("p2", "R2", "F", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val mother = PigeonEntity("p3", "R3", "M", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(pigeon, father, mother))

        val relation = FamilyRelationEntity(
            pigeonId = "p1",
            fatherId = "p2",
            motherId = "p3"
        )
        familyRelationDao.insert(relation)

        val result = familyRelationDao.getByPigeonId("p1")
        assertNotNull(result)
        assertEquals("p2", result!!.fatherId)
        assertEquals("p3", result.motherId)
    }

    @Test
    fun `getChildrenRelations finds children by father or mother`() = runTest {
        val father = PigeonEntity("p1", "R1", "F", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val child1 = PigeonEntity("p2", "R2", "C1", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val child2 = PigeonEntity("p3", "R3", "C2", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(father, child1, child2))

        familyRelationDao.insert(FamilyRelationEntity(pigeonId = "p2", fatherId = "p1"))
        familyRelationDao.insert(FamilyRelationEntity(pigeonId = "p3", motherId = "p1"))

        val children = familyRelationDao.getChildrenRelations("p1")
        assertEquals(2, children.size)
    }

    @Test
    fun `getMateRelations finds relations where pigeon is mate`() = runTest {
        val pigeon1 = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val pigeon2 = PigeonEntity("p2", "R2", "B", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(pigeon1, pigeon2))

        familyRelationDao.insert(FamilyRelationEntity(pigeonId = "p2", mateId = "p1"))

        val mates = familyRelationDao.getMateRelations("p1")
        assertEquals(1, mates.size)
        assertEquals("p2", mates[0].pigeonId)
    }

    @Test
    fun `clearFatherReference removes father reference`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val father = PigeonEntity("p2", "R2", "F", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(pigeon, father))

        familyRelationDao.insert(FamilyRelationEntity(pigeonId = "p1", fatherId = "p2"))
        familyRelationDao.clearFatherReference("p2")

        val result = familyRelationDao.getByPigeonId("p1")
        assertNotNull(result)
        assertNull(result!!.fatherId)
    }

    @Test
    fun `clearMotherReference removes mother reference`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val mother = PigeonEntity("p2", "R2", "M", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(pigeon, mother))

        familyRelationDao.insert(FamilyRelationEntity(pigeonId = "p1", motherId = "p2"))
        familyRelationDao.clearMotherReference("p2")

        assertNull(familyRelationDao.getByPigeonId("p1")!!.motherId)
    }

    @Test
    fun `clearMateReference removes mate reference`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val mate = PigeonEntity("p2", "R2", "B", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(pigeon, mate))

        familyRelationDao.insert(FamilyRelationEntity(pigeonId = "p1", mateId = "p2"))
        familyRelationDao.clearMateReference("p2")

        assertNull(familyRelationDao.getByPigeonId("p1")!!.mateId)
    }

    @Test
    fun `update modifies relation`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val father = PigeonEntity("new-father", "R2", "F", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        pigeonDao.insertAll(listOf(pigeon, father))

        val relation = FamilyRelationEntity(pigeonId = "p1")
        familyRelationDao.insert(relation)

        familyRelationDao.update(relation.copy(fatherId = "new-father"))

        assertEquals("new-father", familyRelationDao.getByPigeonId("p1")!!.fatherId)
    }
}
