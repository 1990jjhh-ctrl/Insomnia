package com.insomnia.diary.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDateTime

@Entity(tableName = "morning_entry")
data class MorningEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAt: LocalDateTime,
    val recovery: Int,
    val inBed: LocalDateTime,
    val outOfBed: LocalDateTime,
    val asleep: LocalDateTime,
    val wokeUp: LocalDateTime,
    val perceivedLatencySec: Long,
    val awakeCount: Int,
    val totalAwakeSec: Long,
    val dreamType: String?,
    val dreamNote: String?,
)

@Entity(
    tableName = "morning_mood",
    foreignKeys = [
        ForeignKey(
            entity = MorningEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId")],
)
data class MorningMoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val label: String,
    val valence: Double,
    val arousal: Double,
)

@Entity(
    tableName = "morning_medication",
    foreignKeys = [
        ForeignKey(
            entity = MorningEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId")],
)
data class MorningMedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val name: String,
    val amount: Double,
    val unit: String,
)

data class MorningEntryFull(
    @Embedded val entry: MorningEntryEntity,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val moods: List<MorningMoodEntity>,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val medication: List<MorningMedicationEntity>,
)
