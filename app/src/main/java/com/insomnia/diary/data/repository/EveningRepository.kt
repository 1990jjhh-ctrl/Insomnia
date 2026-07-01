package com.insomnia.diary.data.repository

import androidx.room.withTransaction
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.db.entity.AttendeeEntity
import com.insomnia.diary.data.db.entity.BatteryCheckpointEntity
import com.insomnia.diary.data.db.entity.DayEventEntity
import com.insomnia.diary.data.db.entity.DayEventWithAttendees
import com.insomnia.diary.data.db.entity.EveningAlcoholEntity
import com.insomnia.diary.data.db.entity.EveningEntryEntity
import com.insomnia.diary.data.db.entity.EveningEntryFull
import com.insomnia.diary.data.db.entity.EveningMoodEntity
import com.insomnia.diary.domain.Attendee
import com.insomnia.diary.domain.BatteryCheckpoint
import com.insomnia.diary.domain.DayEvent
import com.insomnia.diary.domain.EventType
import com.insomnia.diary.domain.EveningProtocol
import com.insomnia.diary.domain.Mood
import com.insomnia.diary.domain.Percentage
import com.insomnia.diary.domain.StressRange
import com.insomnia.diary.domain.Substance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EveningRepository(private val db: InsomniaDatabase) {
    private val dao = db.eveningDao()

    fun observeAll(): Flow<List<EveningProtocol>> =
        dao.observeAll().map { list -> list.map(EveningEntryFull::toDomain) }

    fun observeLatest(): Flow<EveningProtocol?> =
        dao.observeLatest().map { it?.toDomain() }

    suspend fun save(protocol: EveningProtocol) = db.withTransaction {
        val id = dao.insertEntry(EveningEntryEntity(
            recordedAt = protocol.recordedAt,
            productivity = protocol.productivity.value,
        ))
        dao.insertMoods(protocol.moods.map {
            EveningMoodEntity(entryId = id, label = it.label, valence = it.valence, arousal = it.arousal)
        })
        if (protocol.alcohol.isNotEmpty()) {
            dao.insertAlcohol(protocol.alcohol.map {
                EveningAlcoholEntity(entryId = id, name = it.name, amount = it.amount, unit = it.unit)
            })
        }
        protocol.events.forEach { event ->
            val eventId = dao.insertEvent(event.toEntity(id))
            if (event.attendees.isNotEmpty()) {
                dao.insertAttendees(event.attendees.map { AttendeeEntity(eventId = eventId, name = it.name) })
            }
        }
        if (protocol.battery.isNotEmpty()) {
            dao.insertBattery(protocol.battery.map {
                BatteryCheckpointEntity(entryId = id, at = it.at, level = it.level.value)
            })
        }
    }
}

private fun EveningEntryFull.toDomain() = EveningProtocol(
    recordedAt = entry.recordedAt,
    moods = moods.map { Mood(it.label, it.valence, it.arousal) },
    productivity = Percentage(entry.productivity),
    alcohol = alcohol.map { Substance(it.name, it.amount, it.unit) },
    events = events.map(DayEventWithAttendees::toDomain),
    battery = battery.map { BatteryCheckpoint(it.at, Percentage(it.level)) },
)

private fun DayEventWithAttendees.toDomain() = DayEvent(
    type = EventType(event.typeLabel, event.isCustom),
    start = event.start,
    end = event.end,
    stress = StressRange(Percentage(event.stressMin), Percentage(event.stressMax)),
    attendees = attendees.map { Attendee(it.name) },
    note = event.note,
)

private fun DayEvent.toEntity(entryId: Long) = DayEventEntity(
    entryId = entryId,
    typeLabel = type.label,
    isCustom = type.isCustom,
    start = start,
    end = end,
    stressMin = stress.min.value,
    stressMax = stress.max.value,
    note = note,
)
