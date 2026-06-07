package com.pigeonnest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FamilyGroupTest {

    @Test
    fun `displayName uses customName when available`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白",
            matePigeonName = "小红",
            customName = "冠军家族"
        )
        assertEquals("冠军家族", group.displayName)
    }

    @Test
    fun `displayName joins root and mate when no customName`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白",
            matePigeonName = "小红"
        )
        assertEquals("小白 & 小红", group.displayName)
    }

    @Test
    fun `displayName falls back to rootName when no mate`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白",
            matePigeonName = null
        )
        assertEquals("小白", group.displayName)
    }

    @Test
    fun `colorDisplay shows both colors when available`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白",
            rootColor = "白",
            mateColor = "红"
        )
        assertEquals("白 / 红", group.colorDisplay)
    }

    @Test
    fun `colorDisplay falls back to rootColor`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白",
            rootColor = "白",
            mateColor = null
        )
        assertEquals("白", group.colorDisplay)
    }

    @Test
    fun `colorDisplay falls back to mateColor when rootColor missing`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白",
            rootColor = null,
            mateColor = "红"
        )
        assertEquals("红", group.colorDisplay)
    }

    @Test
    fun `colorDisplay returns default when no colors`() {
        val group = FamilyGroup(
            rootPigeonId = "p1",
            rootPigeonName = "小白"
        )
        assertEquals("未记录", group.colorDisplay)
    }
}
