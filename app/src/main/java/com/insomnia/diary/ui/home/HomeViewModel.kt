package com.insomnia.diary.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.repository.EveningRepository
import com.insomnia.diary.data.repository.MorningRepository
import com.insomnia.diary.domain.EveningProtocol
import com.insomnia.diary.domain.MorningProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime

data class HistoryEntry(
    val id: Long,
    val recordedAt: LocalDateTime,
    val label: String,
    val summary: String,
    val isMorning: Boolean,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InsomniaDatabase.getInstance(app)
    private val morningRepo = MorningRepository(db)
    private val eveningRepo = EveningRepository(db)

    val latestMorning: Flow<MorningProtocol?> = morningRepo.observeLatest()
    val latestEvening: Flow<EveningProtocol?> = eveningRepo.observeLatest()

    val history: Flow<List<HistoryEntry>> =
        combine(
            morningRepo.observeAll(),
            eveningRepo.observeAll(),
        ) { morning, evening ->
            val morningEntries =
                morning.mapNotNull { p ->
                    p.id?.let { id ->
                        HistoryEntry(
                            id = id,
                            recordedAt = p.recordedAt,
                            label = "Morning",
                            summary = "${p.recovery.value}% recovery · ${formatDuration(p)}",
                            isMorning = true,
                        )
                    }
                }
            val eveningEntries =
                evening.mapNotNull { p ->
                    p.id?.let { id ->
                        val eventCount = p.events.size
                        HistoryEntry(
                            id = id,
                            recordedAt = p.recordedAt,
                            label = "Evening",
                            summary = "${p.productivity.value}% productivity · $eventCount events",
                            isMorning = false,
                        )
                    }
                }
            (morningEntries + eveningEntries).sortedByDescending { it.recordedAt }
        }
}

internal fun formatDuration(p: MorningProtocol): String {
    val h = p.sleepDuration.toHours()
    val m = p.sleepDuration.toMinutesPart()
    return "${h}h${m}m sleep"
}
