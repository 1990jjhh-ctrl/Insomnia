package com.insomnia.diary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class MorningProtocolTest {
    private val base: LocalDate = LocalDate.of(2024, 1, 15)

    private fun protocol(
        inBed: LocalDateTime = base.atTime(23, 0),
        asleep: LocalDateTime = base.atTime(23, 30),
        wokeUp: LocalDateTime = base.plusDays(1).atTime(7, 0),
        outOfBed: LocalDateTime = base.plusDays(1).atTime(7, 15),
        awakeCount: Int = 0,
        totalAwakeMin: Long = 0L,
    ) = MorningProtocol(
        recordedAt = wokeUp,
        moods = emptyList(),
        recovery = Percentage(80),
        inBed = inBed,
        asleep = asleep,
        wokeUp = wokeUp,
        outOfBed = outOfBed,
        perceivedSleepLatency = Duration.ofMinutes(30),
        awakeEvents = AwakeEvents(awakeCount, Duration.ofMinutes(totalAwakeMin)),
    )

    @Test
    fun timeToFallAsleep_equalsAsleepMinusInBed() {
        assertEquals(Duration.ofMinutes(30), protocol().timeToFallAsleep)
    }

    @Test
    fun timeToFallAsleep_acrossMidnight() {
        val p =
            protocol(
                inBed = base.atTime(23, 45),
                asleep = base.plusDays(1).atTime(0, 15),
            )
        assertEquals(Duration.ofMinutes(30), p.timeToFallAsleep)
    }

    @Test
    fun sleepDuration_noAwakeTime() {
        // wokeUp(07:00) - asleep(23:30) = 7h30m = 450 min
        assertEquals(Duration.ofMinutes(450), protocol().sleepDuration)
    }

    @Test
    fun sleepDuration_subtractsAwakeTime() {
        // 450 min - 20 min awake = 430 min
        assertEquals(Duration.ofMinutes(430), protocol(totalAwakeMin = 20).sleepDuration)
    }

    @Test
    fun init_rejectsAsleepBeforeInBed() {
        assertThrows(IllegalArgumentException::class.java) {
            protocol(
                inBed = base.atTime(23, 30),
                asleep = base.atTime(23, 0),
                wokeUp = base.plusDays(1).atTime(7, 0),
                outOfBed = base.plusDays(1).atTime(7, 15),
            )
        }
    }

    @Test
    fun init_rejectsWokeUpBeforeAsleep() {
        assertThrows(IllegalArgumentException::class.java) {
            protocol(
                asleep = base.atTime(23, 30),
                wokeUp = base.atTime(22, 0),
                outOfBed = base.atTime(22, 15),
            )
        }
    }

    @Test
    fun init_rejectsOutOfBedBeforeWokeUp() {
        assertThrows(IllegalArgumentException::class.java) {
            protocol(
                wokeUp = base.plusDays(1).atTime(7, 30),
                outOfBed = base.plusDays(1).atTime(7, 0),
            )
        }
    }

    @Test
    fun init_allowsEqualTimestamps() {
        val t = base.plusDays(1).atTime(7, 0)
        // Equal timestamps are permitted (e.g. instantly asleep, instant latency).
        val p = protocol(inBed = t, asleep = t, wokeUp = t, outOfBed = t)
        assertEquals(Duration.ZERO, p.timeToFallAsleep)
        assertEquals(Duration.ZERO, p.sleepDuration)
    }
}
