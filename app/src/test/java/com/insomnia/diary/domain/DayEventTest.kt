package com.insomnia.diary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class DayEventTest {

    private val base: LocalDateTime = LocalDateTime.of(2024, 1, 15, 9, 0)
    private val stress = StressRange.flat(Percentage(30))
    private val battery = Percentage(70)

    private fun event(
        preset: EventTypePreset = EventTypePreset.WORK,
        start: LocalDateTime = base,
        end: LocalDateTime = base.plusHours(2),
        batteryLevel: Percentage = battery,
    ) = DayEvent(EventType.from(preset), start, end, stress, batteryLevel)

    @Test
    fun duration_equalsEndMinusStart() {
        assertEquals(Duration.ofHours(2), event().duration)
    }

    @Test
    fun duration_zeroForInstantEvent() {
        assertEquals(Duration.ZERO, event(end = base).duration)
    }

    @Test
    fun init_rejectsEndBeforeStart() {
        assertThrows(IllegalArgumentException::class.java) {
            event(end = base.minusMinutes(1))
        }
    }

    @Test
    fun isNap_trueForNapType() {
        assertTrue(event(preset = EventTypePreset.NAP, end = base.plusHours(1)).isNap)
    }

    @Test
    fun isNap_falseForOtherTypes() {
        assertFalse(event().isNap)
    }

    @Test
    fun batteryLevel_storedOnEveryEvent() {
        assertEquals(Percentage(85), event(batteryLevel = Percentage(85)).batteryLevel)
    }

    @Test
    fun stressRange_rejectsMinGreaterThanMax() {
        assertThrows(IllegalArgumentException::class.java) {
            StressRange(Percentage(60), Percentage(40))
        }
    }

    @Test
    fun eveningProtocol_batteryDerivedFromAllEvents() {
        val e1 = event(start = base, end = base.plusHours(2), batteryLevel = Percentage(80))
        val e2 = event(
            preset = EventTypePreset.MEAL,
            start = base.plusHours(3),
            end = base.plusHours(4),
            batteryLevel = Percentage(60),
        )
        val protocol = EveningProtocol(
            recordedAt = base,
            moods = emptyList(),
            productivity = Percentage(70),
            events = listOf(e1, e2),
        )
        assertEquals(2, protocol.battery.size)
        assertEquals(Percentage(80), protocol.battery[0].level)
        assertEquals(Percentage(60), protocol.battery[1].level)
    }
}
