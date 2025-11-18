package com.mpieterse.stride.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IdsTest {

    @Test
    fun `checkInId is deterministic for same inputs`() {
        val first = checkInId("habit-123", "2025-11-18")
        val second = checkInId("habit-123", "2025-11-18")

        assertEquals(first, second)
    }

    @Test
    fun `checkInId changes when inputs change`() {
        val first = checkInId("habit-123", "2025-11-18")
        val second = checkInId("habit-123", "2025-11-19")

        assertNotEquals(first, second)
    }
}

