package com.pigeonnest.data.local.mapper

import com.pigeonnest.data.local.entity.PigeonEntity
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Loft
import com.pigeonnest.domain.model.PigeonStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PigeonMapperTest {

    private lateinit var mapper: PigeonMapper

    @Before
    fun setup() {
        mapper = PigeonMapper()
    }

    @Test
    fun `toDomain maps entity with correct enum conversions`() {
        val entity = PigeonEntity(
            id = "p1",
            ringNumber = "2024-001",
            name = "小白",
            color = "白",
            gender = 1,
            birthDate = 1700000000000L,
            entryDate = 1700000000000L,
            photoPath = "/photos/p1.jpg",
            eyePhotoPath = "/photos/p1_eye.jpg",
            loftId = "loft-1",
            cageNumber = "A-01",
            status = 0,
            notes = "测试备注",
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L
        )

        val loft = Loft(id = "loft-1", name = "测试鸽舍")
        val domain = mapper.toDomain(entity, loft)

        assertEquals("p1", domain.id)
        assertEquals("2024-001", domain.ringNumber)
        assertEquals("小白", domain.name)
        assertEquals(Gender.MALE, domain.gender)
        assertEquals(PigeonStatus.ACTIVE, domain.status)
        assertEquals(loft, domain.loft)
        assertEquals("A-01", domain.cageNumber)
    }

    @Test
    fun `toDomain handles null loft`() {
        val entity = PigeonEntity(
            id = "p1",
            ringNumber = "2024-001",
            name = "小白",
            gender = 0,
            status = 0,
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L
        )

        val domain = mapper.toDomain(entity, null)
        assertNull(domain.loft)
    }

    @Test
    fun `toDomain maps all gender codes correctly`() {
        val entity = PigeonEntity(
            id = "p1", ringNumber = "R1", name = "A",
            gender = 0, status = 0,
            createdAt = 1L, updatedAt = 1L
        )
        assertEquals(Gender.UNKNOWN, mapper.toDomain(entity).gender)

        val entityMale = entity.copy(gender = 1)
        assertEquals(Gender.MALE, mapper.toDomain(entityMale).gender)

        val entityFemale = entity.copy(gender = 2)
        assertEquals(Gender.FEMALE, mapper.toDomain(entityFemale).gender)
    }

    @Test
    fun `toEntity maps domain with correct enum code extraction`() {
        val loft = Loft(id = "loft-1", name = "测试鸽舍")
        val domain = com.pigeonnest.domain.model.Pigeon(
            id = "p1",
            ringNumber = "2024-001",
            name = "小白",
            color = "白",
            gender = Gender.FEMALE,
            loft = loft,
            status = PigeonStatus.SOLD
        )

        val entity = mapper.toEntity(domain)

        assertEquals("p1", entity.id)
        assertEquals(2, entity.gender)
        assertEquals(1, entity.status)
        assertEquals("loft-1", entity.loftId)
    }

    @Test
    fun `toEntity handles null loft`() {
        val domain = com.pigeonnest.domain.model.Pigeon(
            id = "p1",
            ringNumber = "2024-001",
            name = "小白",
            gender = Gender.UNKNOWN,
            loft = null,
            status = PigeonStatus.ACTIVE
        )

        val entity = mapper.toEntity(domain)
        assertNull(entity.loftId)
    }
}
