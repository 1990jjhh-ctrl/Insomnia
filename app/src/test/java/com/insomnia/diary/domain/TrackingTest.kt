package com.insomnia.diary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TrackingTest {
    @Test
    fun percentage_acceptsZero() {
        assertEquals(0, Percentage(0).value)
    }

    @Test
    fun percentage_acceptsOneHundred() {
        assertEquals(100, Percentage(100).value)
    }

    @Test
    fun percentage_rejectsBelowZero() {
        assertThrows(IllegalArgumentException::class.java) { Percentage(-1) }
    }

    @Test
    fun percentage_rejectsAboveOneHundred() {
        assertThrows(IllegalArgumentException::class.java) { Percentage(101) }
    }

    @Test
    fun stressRange_flat_equalMinAndMax() {
        val s = StressRange.flat(Percentage(40))
        assertEquals(40, s.min.value)
        assertEquals(40, s.max.value)
    }

    @Test
    fun stressRange_allowsEqualMinAndMax() {
        val s = StressRange(Percentage(50), Percentage(50))
        assertEquals(50, s.min.value)
        assertEquals(50, s.max.value)
    }
}
