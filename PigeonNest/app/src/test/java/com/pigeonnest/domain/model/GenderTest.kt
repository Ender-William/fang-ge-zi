package com.pigeonnest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GenderTest {

    @Test
    fun `fromCode returns correct gender for known codes`() {
        assertEquals(Gender.UNKNOWN, Gender.fromCode(0))
        assertEquals(Gender.MALE, Gender.fromCode(1))
        assertEquals(Gender.FEMALE, Gender.fromCode(2))
    }

    @Test
    fun `fromCode returns UNKNOWN for invalid code`() {
        assertEquals(Gender.UNKNOWN, Gender.fromCode(-1))
        assertEquals(Gender.UNKNOWN, Gender.fromCode(99))
    }

    @Test
    fun `gender display names are correct`() {
        assertEquals("未知", Gender.UNKNOWN.displayName)
        assertEquals("雄", Gender.MALE.displayName)
        assertEquals("雌", Gender.FEMALE.displayName)
    }

    @Test
    fun `gender codes are correct`() {
        assertEquals(0, Gender.UNKNOWN.code)
        assertEquals(1, Gender.MALE.code)
        assertEquals(2, Gender.FEMALE.code)
    }
}
