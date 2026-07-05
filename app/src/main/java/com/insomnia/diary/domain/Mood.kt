package com.insomnia.diary.domain

/**
 * A feeling placed on the valence/arousal circumplex.
 *
 * Coordinates: [valence] runs -1 (unpleasant) .. +1 (pleasant); [arousal]
 * runs -1 (calm) .. +1 (activated). An entry may hold several moods, and the
 * user may define custom ones beyond [FeelingPreset]; this type represents
 * both preset-derived and custom moods. Build preset moods with [from].
 */
data class Mood(
    val label: String,
    val valence: Double,
    val arousal: Double,
) {
    init {
        require(valence in -1.0..1.0) { "valence must be in [-1, 1], got $valence" }
        require(arousal in -1.0..1.0) { "arousal must be in [-1, 1], got $arousal" }
    }

    companion object {
        fun from(preset: FeelingPreset): Mood = Mood(preset.label, preset.valence, preset.arousal)
    }
}

/**
 * Named starting points for the mood grid. They seed the reusable mood
 * library and give the user a point to pick or nudge, rather than placing
 * every mood from scratch.
 */
enum class FeelingPreset(
    val label: String,
    val valence: Double,
    val arousal: Double,
) {
    CALM("Calm", 0.5, -0.7),
    RELAXED("Relaxed", 0.5, -0.7),
    CONTENT("Content", 0.8, -0.3),
    ACTIVE("Active", 0.6, 0.8),
    AWAKE("Awake", 0.6, 0.8),
    HAPPY("Happy", 0.9, 0.4),
    TENSE("Tense", -0.6, 0.8),
    STRESSED("Stressed", -0.6, 0.8),
    ANGRY("Angry", -0.8, 0.5),
    FRUSTRATED("Frustrated", -0.8, 0.5),
    EXHAUSTED("Exhausted", -0.3, -0.8),
    SAD("Sad", -0.8, -0.4),
}
