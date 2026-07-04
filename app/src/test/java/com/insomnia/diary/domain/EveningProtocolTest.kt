package com.insomnia.diary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class EveningProtocolTest {

    private val base = LocalDateTime.of(2024, 1, 15, 9, 0)

    private fun protocol(events: List<DayEvent> = emptyList()) = EveningProtocol(
        recordedAt = base,
        moods = emptyList(),
        productivity = Percentage(70),
        events = events,
    )

    private fun event(
        start: LocalDateTime,
        end: LocalDateTime,
        battery: Int = 70,
    ) = DayEvent(
        type = EventType.from(EventTypePreset.WORK),
        start = start,
        end = end,
        stress = StressRange.flat(Percentage(30)),
        batteryLevel = Percentage(battery),
    )

    @Test
    fun battery_emptyWhenNoEvents() {
        assertTrue(protocol().battery.isEmpty())
    }

    @Test
    fun battery_sortedByStartRegardlessOfInsertionOrder() {
        // Events inserted later-first; battery must be sorted by time.
        val later = event(base.plusHours(3), base.plusHours(4), battery = 50)
        val earlier = event(base, base.plusHours(2), battery = 80)
        val checkpoints = protocol(listOf(later, earlier)).battery

        assertEquals(2, checkpoints.size)
        assertEquals(Percentage(80), checkpoints[0].level)
        assertEquals(Percentage(50), checkpoints[1].level)
    }

    @Test
    fun battery_timestampMatchesEventStart() {
        val start = base.plusHours(1)
        val ev = event(start, start.plusHours(1), battery = 65)
        val checkpoint = protocol(listOf(ev)).battery.single()

        assertEquals(start, checkpoint.at)
        assertEquals(Percentage(65), checkpoint.level)
    }
}
