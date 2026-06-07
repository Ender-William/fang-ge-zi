package com.pigeonnest.data.repository

import com.pigeonnest.data.local.dao.FamilyRelationDao
import com.pigeonnest.data.local.dao.PigeonDao
import com.pigeonnest.data.local.entity.FamilyRelationEntity
import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.data.local.mapper.PigeonMapper
import com.pigeonnest.domain.model.Gender
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyRepositoryImplTest {

    private val pigeonDao: PigeonDao = mockk()
    private val familyRelationDao: FamilyRelationDao = mockk()
    private val pigeonMapper = PigeonMapper()
    private val repository = FamilyRepositoryImpl(pigeonDao, familyRelationDao, pigeonMapper)

    init {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @Test
    fun `getFamilyRelation builds complete relation`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val father = PigeonEntity("p2", "R2", "大白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val mother = PigeonEntity("p3", "R3", "大红", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        val child = PigeonEntity("p4", "R4", "小小白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)

        coEvery { familyRelationDao.getByPigeonId("p1") } returns FamilyRelationEntity(
            pigeonId = "p1", fatherId = "p2", motherId = "p3"
        )
        coEvery { familyRelationDao.getChildrenRelations("p1") } returns listOf(
            FamilyRelationEntity(pigeonId = "p4", fatherId = "p1")
        )
        coEvery { pigeonDao.getById("p1") } returns pigeon
        coEvery { pigeonDao.getById("p2") } returns father
        coEvery { pigeonDao.getById("p3") } returns mother
        coEvery { pigeonDao.getById("p4") } returns child

        val result = repository.getFamilyRelation("p1")

        assertNotNull(result)
        assertEquals("大白", result!!.father!!.name)
        assertEquals("大红", result.mother!!.name)
        assertEquals(1, result.children.size)
        assertEquals("小小白", result.children[0].name)
    }

    @Test
    fun `getFamilyRelation returns null when no relations`() = runTest {
        coEvery { familyRelationDao.getByPigeonId("p1") } returns null
        coEvery { familyRelationDao.getChildrenRelations("p1") } returns emptyList()
        coEvery { pigeonDao.getById("p1") } returns null

        val result = repository.getFamilyRelation("p1")
        assertNull(result)
    }

    @Test
    fun `updateParents inserts new relation when none exists`() = runTest {
        coEvery { familyRelationDao.getByPigeonId("p1") } returns null
        coEvery { familyRelationDao.insert(any()) } returns Unit

        val result = repository.updateParents("p1", "father-1", "mother-1")

        assertTrue(result.isSuccess)
        coVerify { familyRelationDao.insert(any()) }
    }

    @Test
    fun `updateParents updates existing relation`() = runTest {
        val existing = FamilyRelationEntity(pigeonId = "p1")
        coEvery { familyRelationDao.getByPigeonId("p1") } returns existing
        coEvery { familyRelationDao.update(any()) } returns Unit

        val result = repository.updateParents("p1", "father-1", null)

        assertTrue(result.isSuccess)
        coVerify { familyRelationDao.update(any()) }
    }

    @Test
    fun `updateMate clears old mate bidirectionally and sets new mate`() = runTest {
        val existingRelation = FamilyRelationEntity(pigeonId = "p1", mateId = "old-mate")
        val oldMateRelation = FamilyRelationEntity(pigeonId = "old-mate", mateId = "p1")
        val newMateRelation = FamilyRelationEntity(pigeonId = "new-mate", mateId = null)

        coEvery { familyRelationDao.getByPigeonId("p1") } returns existingRelation
        coEvery { familyRelationDao.getByPigeonId("old-mate") } returns oldMateRelation
        coEvery { familyRelationDao.getByPigeonId("new-mate") } returns newMateRelation
        coEvery { familyRelationDao.update(any()) } returns Unit
        coEvery { familyRelationDao.insert(any()) } returns Unit

        val result = repository.updateMate("p1", "new-mate")

        assertTrue(result.isSuccess)
        // Old mate should be cleared
        coVerify { familyRelationDao.update(oldMateRelation.copy(mateId = null)) }
        // New mate should be set to p1
        coVerify { familyRelationDao.update(newMateRelation.copy(mateId = "p1")) }
    }

    @Test
    fun `getLineage builds tree with parent inference`() = runTest {
        val child = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val father = PigeonEntity("p2", "R2", "大白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val inferredMother = PigeonEntity("p3", "R3", "大红", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)

        // Child has father but no mother
        coEvery { familyRelationDao.getByPigeonId("p1") } returns FamilyRelationEntity(
            pigeonId = "p1", fatherId = "p2"
        )
        // Father has a mate (the inferred mother)
        coEvery { familyRelationDao.getByPigeonId("p2") } returns FamilyRelationEntity(
            pigeonId = "p2", mateId = "p3"
        )
        // Mother's relation
        coEvery { familyRelationDao.getByPigeonId("p3") } returns null

        coEvery { pigeonDao.getById("p1") } returns child
        coEvery { pigeonDao.getById("p2") } returns father
        coEvery { pigeonDao.getById("p3") } returns inferredMother

        val result = repository.getLineage("p1", generations = 3)

        assertEquals("小白", result.pigeon.name)
        assertNotNull(result.father)
        assertEquals("大白", result.father!!.pigeon.name)
        // Mother should be inferred from father's mate
        assertNotNull(result.mother)
        assertEquals("大红", result.mother!!.pigeon.name)
    }

    @Test
    fun `getSiblings returns correct siblings`() = runTest {
        val pigeon = PigeonEntity("p1", "R1", "A", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val sibling = PigeonEntity("p2", "R2", "B", gender = 2, status = 0, createdAt = 1L, updatedAt = 1L)
        val unrelated = PigeonEntity("p3", "R3", "C", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)

        coEvery { familyRelationDao.getByPigeonId("p1") } returns FamilyRelationEntity(
            pigeonId = "p1", fatherId = "f1"
        )
        coEvery { familyRelationDao.getChildrenRelations("f1") } returns listOf(
            FamilyRelationEntity(pigeonId = "p1", fatherId = "f1"),
            FamilyRelationEntity(pigeonId = "p2", fatherId = "f1")
        )
        coEvery { pigeonDao.getById("p2") } returns sibling

        val siblings = repository.getSiblings("p1")

        assertEquals(1, siblings.size)
        assertEquals("B", siblings[0].name)
    }

    @Test
    fun `getGraphData builds nodes and edges`() = runTest {
        val center = PigeonEntity("p1", "R1", "小白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val father = PigeonEntity("p2", "R2", "大白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)
        val child = PigeonEntity("p3", "R3", "小小白", gender = 1, status = 0, createdAt = 1L, updatedAt = 1L)

        coEvery { pigeonDao.getById("p1") } returns center
        coEvery { pigeonDao.getById("p2") } returns father
        coEvery { pigeonDao.getById("p3") } returns child
        coEvery { familyRelationDao.getByPigeonId("p1") } returns FamilyRelationEntity(
            pigeonId = "p1", fatherId = "p2"
        )
        coEvery { familyRelationDao.getChildrenRelations("p1") } returns listOf(
            FamilyRelationEntity(pigeonId = "p3", fatherId = "p1")
        )
        coEvery { familyRelationDao.getByPigeonId("p2") } returns null
        coEvery { familyRelationDao.getChildrenRelations("p2") } returns emptyList()
        coEvery { familyRelationDao.getByPigeonId("p3") } returns null
        coEvery { familyRelationDao.getChildrenRelations("p3") } returns emptyList()

        val graphData = repository.getGraphData("p1", depth = 3)

        assertEquals("p1", graphData.rootNode.pigeonId)
        assertTrue(graphData.allNodes.size >= 3)
        assertTrue(graphData.edges.isNotEmpty())
    }
}
