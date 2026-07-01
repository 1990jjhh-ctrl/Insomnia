package com.insomnia.diary.ui.morning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.repository.MorningRepository
import com.insomnia.diary.domain.AwakeEvents
import com.insomnia.diary.domain.Dream
import com.insomnia.diary.domain.DreamRecall
import com.insomnia.diary.domain.Mood
import com.insomnia.diary.domain.MorningProtocol
import com.insomnia.diary.domain.Percentage
import com.insomnia.diary.domain.Substance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class MorningFormState(
    val moods: List<Mood> = emptyList(),
    val recovery: Int = 70,
    val inBed: LocalDateTime = LocalDate.now().minusDays(1).atTime(23, 0),
    val asleep: LocalDateTime = LocalDate.now().minusDays(1).atTime(23, 30),
    val wokeUp: LocalDateTime = LocalDate.now().atTime(LocalTime.now().hour, LocalTime.now().minute),
    val outOfBed: LocalDateTime = LocalDate.now().atTime(LocalTime.now().hour, LocalTime.now().minute),
    val perceivedLatencyMin: Int = 15,
    val awakeCount: Int = 0,
    val totalAwakeMin: Int = 0,
    val medication: List<Substance> = emptyList(),
    val dreamType: Dream? = null,
    val dreamNote: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
)

class MorningViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MorningRepository(InsomniaDatabase.getInstance(app))
    private val _state = MutableStateFlow(MorningFormState())
    val state: StateFlow<MorningFormState> = _state.asStateFlow()

    fun toggleMood(mood: Mood) = _state.update { s ->
        val exists = s.moods.any { it.label == mood.label }
        s.copy(moods = if (exists) s.moods.filter { it.label != mood.label } else s.moods + mood)
    }

    fun setRecovery(v: Int) = _state.update { it.copy(recovery = v) }
    fun setInBed(v: LocalDateTime) = _state.update { it.copy(inBed = v) }
    fun setAsleep(v: LocalDateTime) = _state.update { it.copy(asleep = v) }
    fun setWokeUp(v: LocalDateTime) = _state.update { it.copy(wokeUp = v) }
    fun setOutOfBed(v: LocalDateTime) = _state.update { it.copy(outOfBed = v) }
    fun setPerceivedLatency(v: Int) = _state.update { it.copy(perceivedLatencyMin = v.coerceIn(0, 240)) }
    fun setAwakeCount(v: Int) = _state.update { it.copy(awakeCount = v.coerceAtLeast(0)) }
    fun setTotalAwake(v: Int) = _state.update { it.copy(totalAwakeMin = v.coerceAtLeast(0)) }
    fun addMedication(s: Substance) = _state.update { it.copy(medication = it.medication + s) }
    fun removeMedication(i: Int) = _state.update { it.copy(medication = it.medication.toMutableList().also { l -> l.removeAt(i) }) }
    fun setDreamType(d: Dream?) = _state.update { it.copy(dreamType = d) }
    fun setDreamNote(n: String) = _state.update { it.copy(dreamNote = n) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.save(
                MorningProtocol(
                    recordedAt = LocalDateTime.now(),
                    moods = s.moods,
                    recovery = Percentage(s.recovery),
                    inBed = s.inBed,
                    outOfBed = s.outOfBed,
                    asleep = s.asleep,
                    wokeUp = s.wokeUp,
                    perceivedSleepLatency = Duration.ofMinutes(s.perceivedLatencyMin.toLong()),
                    awakeEvents = AwakeEvents(s.awakeCount, Duration.ofMinutes(s.totalAwakeMin.toLong())),
                    medication = s.medication,
                    dream = s.dreamType?.let { DreamRecall(it, s.dreamNote.ifBlank { null }) },
                )
            )
            _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}
