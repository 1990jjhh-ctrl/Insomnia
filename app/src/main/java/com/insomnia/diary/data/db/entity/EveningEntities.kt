package com.insomnia.diary.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDateTime

@Entity(tableName = "evening_entry")
data class EveningEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAt: LocalDateTime,
    val productivity: Int,
)

@Entity(
    tableName = "evening_mood",
    foreignKeys = [ForeignKey(
        entity = EveningEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("entryId")],
)
data class EveningMoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val label: String,
    val valence: Double,
    val arousal: Double,
)

@Entity(
    tableName = "evening_alcohol",
    foreignKeys = [ForeignKey(
        entity = EveningEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("entryId")],
)
data class EveningAlcoholEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val name: String,
    val amount: Double,
    val unit: String,
)

@Entity(
    tableName = "day_event",
    foreignKeys = [ForeignKey(
        entity = EveningEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("entryId")],
)
data class DayEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val typeLabel: String,
    val isCustom: Boolean,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val stressMin: Int,
    val stressMax: Int,
    val note: String?,
    val batteryLevel: Int? = null,
)

@Entity(
    tableName = "event_attendee",
    foreignKeys = [ForeignKey(
        entity = DayEventEntity::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("eventId")],
)
data class AttendeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val name: String,
)

data class DayEventWithAttendees(
    @Embedded val event: DayEventEntity,
    @Relation(parentColumn = "id", entityColumn = "eventId")
    val attendees: List<AttendeeEntity>,
)

@Entity(
    tableName = "custom_event_type",
    indices = [Index(value = ["label"], unique = true)],
)
data class CustomEventTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
)

@Entity(
    tableName = "custom_mood",
    indices = [Index(value = ["label"], unique = true)],
)
data class CustomMoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val valence: Double = 0.0,
    val arousal: Double = 0.0,
)

data class EveningEntryFull(
    @Embedded val entry: EveningEntryEntity,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val moods: List<EveningMoodEntity>,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val alcohol: List<EveningAlcoholEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId",
        entity = DayEventEntity::class,
    )
    val events: List<DayEventWithAttendees>,
)
