package com.insomnia.diary.domain

import java.time.Duration
import java.time.LocalDateTime

/**
 * A stress level for an event, given as a range. Use [flat] when the level
 * was steady. Both bounds are 0..100%; [min] may not exceed [max].
 */
data class StressRange(
    val min: Percentage,
    val max: Percentage,
) {
    init {
        require(min.value <= max.value) {
            "stress min ${min.value} must be <= max ${max.value}"
        }
    }

    companion object {
        fun flat(level: Percentage): StressRange = StressRange(level, level)
    }
}

/**
 * Standard event types seeding the reusable event-type library. The user
 * picks one of these or adds a custom type via [EventType.custom].
 */
enum class EventTypePreset(val label: String) {
    WORK("Work"),
    COMMUTE("Commute"),
    MEETING("Meeting"),
    MEAL("Meal"),
    SHOWER("Shower"),
    WALK("Walk"),
    TRAVEL("Travel"),
    SOCIAL("Social"),
    CHORES("Chores"),
    NAP("Nap"),
}

/**
 * The kind of a [DayEvent]. Build standard kinds with [from] and ad-hoc user
 * kinds with [custom]; [isCustom] lets the library show which are the user's
 * own. [NAP] is exposed so sleep stats can find naps by type.
 */
data class EventType(
    val label: String,
    val isCustom: Boolean = false,
) {
    companion object {
        val NAP: EventType = from(EventTypePreset.NAP)

        fun from(preset: EventTypePreset): EventType = EventType(preset.label)

        fun custom(label: String): EventType = EventType(label, isCustom = true)
    }
}

/** A person present at an event, for "who drains me" correlations. */
data class Attendee(val name: String)

/**
 * One entry on the day's timeline: something done over a time span, with the
 * [stress] it carried and who was there. Energy is not stored here; it is
 * derived from battery checkpoints over [start]..[end].
 */
data class DayEvent(
    val type: EventType,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val stress: StressRange,
    val attendees: List<Attendee> = emptyList(),
    val note: String? = null,
) {
    init {
        require(!end.isBefore(start)) { "event end must not be before start" }
    }

    val duration: Duration get() = Duration.between(start, end)

    val isNap: Boolean get() = type == EventType.NAP
}
