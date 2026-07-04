package com.insomnia.diary.domain

import java.time.Duration
import java.time.LocalDateTime

/**
 * Shared shape of every diary protocol. [id] is null for unsaved entries and
 * populated when loaded from storage.
 */
sealed interface Protocol {
    val id: Long?
    val recordedAt: LocalDateTime
    val moods: List<Mood>
}

/**
 * The morning protocol, recording the night just passed.
 *
 * Timestamps are absolute (date + time) so a night crossing midnight needs
 * no special handling. The ordering invariant inBed ≤ asleep ≤ wokeUp ≤
 * outOfBed is enforced in [init].
 */
data class MorningProtocol(
    override val id: Long? = null,
    override val recordedAt: LocalDateTime,
    override val moods: List<Mood>,
    val recovery: Percentage,
    val inBed: LocalDateTime,
    val outOfBed: LocalDateTime,
    val asleep: LocalDateTime,
    val wokeUp: LocalDateTime,
    val perceivedSleepLatency: Duration,
    val awakeEvents: AwakeEvents,
    val medication: List<Substance> = emptyList(),
    val dream: DreamRecall? = null,
) : Protocol {
    init {
        require(!asleep.isBefore(inBed)) { "asleep must not be before inBed" }
        require(!wokeUp.isBefore(asleep)) { "wokeUp must not be before asleep" }
        require(!outOfBed.isBefore(wokeUp)) { "outOfBed must not be before wokeUp" }
    }

    /** Latency from getting into bed to falling asleep. */
    val timeToFallAsleep: Duration get() = Duration.between(inBed, asleep)

    /** Time actually asleep: the sleep span minus time spent awake. */
    val sleepDuration: Duration
        get() = Duration.between(asleep, wokeUp).minus(awakeEvents.totalAwake)
}

/**
 * The evening protocol, recording the day just passed.
 *
 * The day is captured as a timeline of [events]; [battery] is a convenience
 * view of those events ordered by time with their energy levels.
 */
data class EveningProtocol(
    override val id: Long? = null,
    override val recordedAt: LocalDateTime,
    override val moods: List<Mood>,
    val productivity: Percentage,
    val alcohol: List<Substance> = emptyList(),
    val events: List<DayEvent> = emptyList(),
) : Protocol {
    val battery: List<BatteryCheckpoint>
        get() = events.sortedBy { it.start }.map { BatteryCheckpoint(it.start, it.batteryLevel) }
}
