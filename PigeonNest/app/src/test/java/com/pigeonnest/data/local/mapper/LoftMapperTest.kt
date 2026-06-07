package com.pigeonnest.data.local.mapper

import com.pigeonnest.data.local.entity.LoftEntity
import com.pigeonnest.domain.model.Loft
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoftMapperTest {

    private lateinit var mapper: LoftMapper

    @Before
    fun setup() {
        mapper = LoftMapper()
    }

    @Test
    fun `toDomain injects pigeonCount`() {
        val entity = LoftEntity(
            id = "loft-1",
            name = "测试鸽舍",
            location = "屋顶",
            description = "主鸽舍",
            capacity = 50,
            colorTag = "#FF0000",
            sortOrder = 1
        )

        val domain = mapper.toDomain(entity, pigeonCount = 12)

        assertEquals("loft-1", domain.id)
        assertEquals("测试鸽舍", domain.name)
        assertEquals(12, domain.pigeonCount)
        assertEquals(1, domain.sortOrder)
    }

    @Test
    fun `toDomain defaults pigeonCount to zero`() {
        val entity = LoftEntity(
            id = "loft-1",
            name = "测试鸽舍",
            sortOrder = 0
        )

        val domain = mapper.toDomain(entity)
        assertEquals(0, domain.pigeonCount)
    }

    @Test
    fun `toEntity strips pigeonCount`() {
        val domain = Loft(
            id = "loft-1",
            name = "测试鸽舍",
            location = "屋顶",
            description = "主鸽舍",
            capacity = 50,
            colorTag = "#FF0000",
            sortOrder = 1,
            pigeonCount = 99
        )

        val entity = mapper.toEntity(domain)

        assertEquals("loft-1", entity.id)
        assertEquals("测试鸽舍", entity.name)
        assertEquals(1, entity.sortOrder)
        // pigeonCount is not part of entity
    }
}
