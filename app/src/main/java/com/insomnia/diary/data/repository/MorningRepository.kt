package com.insomnia.diary.data.repository

import androidx.room.withTransaction
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.db.entity.MorningEntryEntity
import com.insomnia.diary.data.db.entity.MorningEntryFull
import com.insomnia.diary.data.db.entity.MorningMedicationEntity
import com.insomnia.diary.data.db.entity.MorningMoodEntity
import com.insomnia.diary.domain.AwakeEvents
import com.insomnia.diary.domain.Dream
import com.insomnia.diary.domain.DreamRecall
import com.insomnia.diary.domain.Mood
import com.insomnia.diary.domain.MorningProtocol
import com.insomnia.diary.domain.Percentage
import com.insomnia.diary.domain.Substance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration

class MorningRepository(private val db: InsomniaDatabase) {
    private val dao = db.morningDao()

    fun observeAll(): Flow<List<MorningProtocol>> =
        dao.observeAll().map { list -> list.map(MorningEntryFull::toDomain) }

    fun observeLatest(): Flow<MorningProtocol?> = dao.observeLatest().map { it?.toDomain() }

    suspend fun findById(id: Long): MorningProtocol? = dao.findById(id)?.toDomain()

    suspend fun save(protocol: MorningProtocol) =
        db.withTransaction {
            insertAll(protocol)
        }

    suspend fun update(
        id: Long,
        protocol: MorningProtocol,
    ) = db.withTransaction {
        dao.deleteById(id)
        insertAll(protocol)
    }

    private suspend fun insertAll(protocol: MorningProtocol) {
        val id = dao.insertEntry(protocol.toEntity())
        dao.insertMoods(protocol.moods.map { it.toEntity(id) })
        if (protocol.medication.isNotEmpty()) {
            dao.insertMedication(protocol.medication.map { it.toMedicationEntity(id) })
        }
    }
}

private fun MorningEntryFull.toDomain() =
    MorningProtocol(
        id = entry.id,
        recordedAt = entry.recordedAt,
        moods = moods.map { Mood(it.label, it.valence, it.arousal) },
        recovery = Percentage(entry.recovery),
        inBed = entry.inBed,
        outOfBed = entry.outOfBed,
        asleep = entry.asleep,
        wokeUp = entry.wokeUp,
        perceivedSleepLatency = Duration.ofSeconds(entry.perceivedLatencySec),
        awakeEvents = AwakeEvents(entry.awakeCount, Duration.ofSeconds(entry.totalAwakeSec)),
        medication = medication.map { Substance(it.name, it.amount, it.unit) },
        dream =
            entry.dreamType?.let { typeName ->
                Dream.entries.find { it.name == typeName }?.let { DreamRecall(it, entry.dreamNote) }
            },
    )

private fun MorningProtocol.toEntity() =
    MorningEntryEntity(
        recordedAt = recordedAt,
        recovery = recovery.value,
        inBed = inBed,
        outOfBed = outOfBed,
        asleep = asleep,
        wokeUp = wokeUp,
        perceivedLatencySec = perceivedSleepLatency.seconds,
        awakeCount = awakeEvents.count,
        totalAwakeSec = awakeEvents.totalAwake.seconds,
        dreamType = dream?.type?.name,
        dreamNote = dream?.note,
    )

private fun Mood.toEntity(entryId: Long) =
    MorningMoodEntity(entryId = entryId, label = label, valence = valence, arousal = arousal)

private fun Substance.toMedicationEntity(entryId: Long) =
    MorningMedicationEntity(entryId = entryId, name = name, amount = amount, unit = unit)
