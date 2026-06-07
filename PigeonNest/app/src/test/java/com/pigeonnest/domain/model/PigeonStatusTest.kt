package com.pigeonnest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PigeonStatusTest {

    @Test
    fun `fromCode returns correct status for known codes`() {
        assertEquals(PigeonStatus.ACTIVE, PigeonStatus.fromCode(0))
        assertEquals(PigeonStatus.SOLD, PigeonStatus.fromCode(1))
        assertEquals(PigeonStatus.DECEASED, PigeonStatus.fromCode(2))
        assertEquals(PigeonStatus.GIFTED, PigeonStatus.fromCode(3))
    }

    @Test
    fun `fromCode returns ACTIVE for invalid code`() {
        assertEquals(PigeonStatus.ACTIVE, PigeonStatus.fromCode(-1))
        assertEquals(PigeonStatus.ACTIVE, PigeonStatus.fromCode(99))
    }

    @Test
    fun `status display names are correct`() {
        assertEquals("在养", PigeonStatus.ACTIVE.displayName)
        assertEquals("已售", PigeonStatus.SOLD.displayName)
        assertEquals("已故", PigeonStatus.DECEASED.displayName)
        assertEquals("赠送", PigeonStatus.GIFTED.displayName)
    }
}
