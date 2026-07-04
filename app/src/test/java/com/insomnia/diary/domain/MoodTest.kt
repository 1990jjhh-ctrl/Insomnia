package com.insomnia.diary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoodTest {

    @Test
    fun mood_acceptsValidCoordinates() {
        val m = Mood("neutral", 0.0, 0.0)
        assertEquals(0.0, m.valence, 0.0)
        assertEquals(0.0, m.arousal, 0.0)
    }

    @Test
    fun mood_acceptsEdgeBoundaries() {
        Mood("low", -1.0, -1.0)
        Mood("high", 1.0, 1.0)
    }

    @Test
    fun mood_rejectsValenceBelowMinus1() {
        assertThrows(IllegalArgumentException::class.java) {
            Mood("x", -1.001, 0.0)
        }
    }

    @Test
    fun mood_rejectsValenceAbove1() {
        assertThrows(IllegalArgumentException::class.java) {
            Mood("x", 1.001, 0.0)
        }
    }

    @Test
    fun mood_rejectsArousalBelowMinus1() {
        assertThrows(IllegalArgumentException::class.java) {
            Mood("x", 0.0, -1.001)
        }
    }

    @Test
    fun mood_rejectsArousalAbove1() {
        assertThrows(IllegalArgumentException::class.java) {
            Mood("x", 0.0, 1.001)
        }
    }

    @Test
    fun mood_fromPreset_copiesLabelAndCoordinates() {
        val m = Mood.from(FeelingPreset.HAPPY)
        assertEquals(FeelingPreset.HAPPY.label, m.label)
        assertEquals(FeelingPreset.HAPPY.valence, m.valence, 0.0)
        assertEquals(FeelingPreset.HAPPY.arousal, m.arousal, 0.0)
    }

    @Test
    fun feelingPreset_allPresetsHaveValidCoordinates() {
        // Guards against a preset being added with out-of-range values.
        FeelingPreset.entries.forEach { Mood.from(it) }
    }
}
