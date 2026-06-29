package com.insomnia.diary.domain

import java.time.Duration
import java.time.LocalDateTime

/**
 * A subjective intensity on the shared 0..100% scale (recovery,
 * productivity, exhaustion, stress). Out-of-range input fails fast, so an
 * invalid percentage can never reach storage.
 */
@JvmInline
value class Percentage(val value: Int) {
    init {
        require(value in 0..100) { "percentage must be in [0, 100], got $value" }
    }
}

/**
 * A consumed substance (alcohol or medication), stored losslessly as a raw
 * [amount] in its own [unit] (e.g. 0.5 "L", 400 "mg"). Correlation uses the
 * raw amount per named substance; no normalisation is applied here.
 */
data class Substance(
    val name: String,
    val amount: Double,
    val unit: String,
)

/** The kind of dream recalled on waking, for the morning protocol. */
enum class Dream(val label: String) {
    NIGHTMARE("Nightmare"),
    ANXIETY("Anxiety Dream"),
    PROCESSING("Processing Dream"),
    ABSTRACT("Abstract Dream"),
    LUCID("Lucid Dream"),
    NO_MEMORY("No Memory"),
}

/** A recalled dream: its [type] plus an optional free-text [note]. */
data class DreamRecall(
    val type: Dream,
    val note: String? = null,
)

/**
 * Night-time waking summary: how many times the user woke ([count]) and the
 * [totalAwake] time, which is subtracted from the sleep span to yield the
 * time actually asleep.
 */
data class AwakeEvents(
    val count: Int,
    val totalAwake: Duration,
)

/**
 * A timestamped reading of battery (energy) remaining. The user logs a few
 * across the day; per-interval drain or recharge is derived between them, and
 * the last reading of the day stands in for end-of-day exhaustion.
 */
data class BatteryCheckpoint(
    val at: LocalDateTime,
    val level: Percentage,
)
