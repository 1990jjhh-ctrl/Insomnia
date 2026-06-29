package com.insomnia.diary.domain

import java.time.Duration
import java.time.LocalDateTime

/**
 * Shared shape of every diary protocol.
 *
 * Being a sealed interface, a `when` over a [Protocol] is exhaustive: adding
 * a protocol forces every handler to acknowledge it. This is the Kotlin
 * replacement for the prototype's runtime entry-type registry.
 */
sealed interface Protocol {
    /** When the protocol was filled in. */
    val recordedAt: LocalDateTime

    /** Moods felt at the time (on waking, or before bed). */
    val moods: List<Mood>
}

/**
 * The morning protocol, recording the night just passed.
 *
 * Timestamps are absolute (date + time), so a night crossing midnight needs
 * no special handling and the ordering can be validated by callers.
 */
data class MorningProtocol(
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
    /** Latency from getting into bed to falling asleep. */
    val timeToFallAsleep: Duration get() = Duration.between(inBed, asleep)

    /** Time actually asleep: the sleep span minus time spent awake. */
    val sleepDuration: Duration
        get() = Duration.between(asleep, wokeUp).minus(awakeEvents.totalAwake)
}

/**
 * The evening protocol, recording the day just passed.
 *
 * The day is captured as a timeline of [events] plus [battery] checkpoints;
 * stress and energy are derived from these (see docs/PRD.md). Day context
 * (day-type, location, tags) is still to be added.
 */
data class EveningProtocol(
    override val recordedAt: LocalDateTime,
    override val moods: List<Mood>,
    val productivity: Percentage,
    val alcohol: List<Substance> = emptyList(),
    val events: List<DayEvent> = emptyList(),
    val battery: List<BatteryCheckpoint> = emptyList(),
) : Protocol
