package com.insomnia.diary.ui.morning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.repository.EveningRepository
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
import java.time.temporal.ChronoUnit

data class MorningFormState(
    val moods: List<Mood> = emptyList(),
    val customMoods: List<Mood> = emptyList(),
    val recovery: Int = 70,
    val inBed: LocalDateTime = LocalDate.now().minusDays(1).atTime(23, 0),
    val asleep: LocalDateTime = LocalDate.now().minusDays(1).atTime(23, 30),
    val wokeUp: LocalDateTime = LocalDate.now().atTime(LocalTime.now().hour, LocalTime.now().minute),
    val outOfBed: LocalDateTime = LocalDate.now().atTime(LocalTime.now().hour, LocalTime.now().minute),
    val awakeCount: Int = 0,
    val totalAwakeMin: Int = 0,
    val medication: List<Substance> = emptyList(),
    val dreamType: Dream? = null,
    val dreamNote: String = "",
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
) {
    val date: LocalDate get() = wokeUp.toLocalDate()
    val wentToBedAfterMidnight: Boolean get() = inBed.toLocalDate() == wokeUp.toLocalDate()

    companion object {
        fun from(p: MorningProtocol) = MorningFormState(
            moods = p.moods,
            recovery = p.recovery.value,
            inBed = p.inBed,
            asleep = p.asleep,
            wokeUp = p.wokeUp,
            outOfBed = p.outOfBed,
            awakeCount = p.awakeEvents.count,
            totalAwakeMin = p.awakeEvents.totalAwake.toMinutes().toInt(),
            medication = p.medication,
            dreamType = p.dream?.type,
            dreamNote = p.dream?.note ?: "",
            recordedAt = p.recordedAt,
        )
    }
}

class MorningViewModel(app: Application, private val entryId: Long?) : AndroidViewModel(app) {
    val isEditing: Boolean get() = entryId != null
    private val repo = MorningRepository(InsomniaDatabase.getInstance(app))
    private val eveningRepo = EveningRepository(InsomniaDatabase.getInstance(app))
    private val _state = MutableStateFlow(MorningFormState())
    val state: StateFlow<MorningFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            eveningRepo.observeCustomMoods().collect { moods ->
                _state.update { it.copy(customMoods = moods) }
            }
        }
        if (entryId != null) {
            viewModelScope.launch {
                repo.findById(entryId)?.let { p -> _state.update { MorningFormState.from(p) } }
            }
        }
    }

    fun toggleMood(mood: Mood) = _state.update { s ->
        val exists = s.moods.any { it.label == mood.label }
        s.copy(moods = if (exists) s.moods.filter { it.label != mood.label } else s.moods + mood)
    }

    fun setDate(d: LocalDate) = _state.update { s ->
        val delta = ChronoUnit.DAYS.between(s.date, d)
        if (delta == 0L) return@update s
        s.copy(
            inBed = s.inBed.plusDays(delta),
            asleep = s.asleep.plusDays(delta),
            wokeUp = s.wokeUp.plusDays(delta),
            outOfBed = s.outOfBed.plusDays(delta),
        )
    }

    fun setRecovery(v: Int) = _state.update { it.copy(recovery = v) }
    fun setInBed(v: LocalDateTime) = _state.update { it.copy(inBed = v) }
    fun setAsleep(v: LocalDateTime) = _state.update { it.copy(asleep = v) }
    fun setWokeUp(v: LocalDateTime) = _state.update { it.copy(wokeUp = v) }
    fun setOutOfBed(v: LocalDateTime) = _state.update { it.copy(outOfBed = v) }

    fun toggleAfterMidnight() = _state.update { s ->
        val shift = if (s.wentToBedAfterMidnight) -1L else 1L
        s.copy(inBed = s.inBed.plusDays(shift), asleep = s.asleep.plusDays(shift))
    }

    fun setAwakeCount(v: Int) = _state.update { it.copy(awakeCount = v.coerceAtLeast(0)) }
    fun setTotalAwake(v: Int) = _state.update { it.copy(totalAwakeMin = v.coerceAtLeast(0)) }
    fun addMedication(s: Substance) = _state.update { it.copy(medication = it.medication + s) }
    fun removeMedication(i: Int) =
        _state.update { it.copy(medication = it.medication.toMutableList().also { l -> l.removeAt(i) }) }
    fun setDreamType(d: Dream?) = _state.update { it.copy(dreamType = d) }
    fun setDreamNote(n: String) = _state.update { it.copy(dreamNote = n) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val protocol = MorningProtocol(
                recordedAt = if (entryId != null) s.recordedAt else LocalDateTime.now(),
                moods = s.moods,
                recovery = Percentage(s.recovery),
                inBed = s.inBed,
                outOfBed = s.outOfBed,
                asleep = s.asleep,
                wokeUp = s.wokeUp,
                perceivedSleepLatency = Duration.between(s.inBed, s.asleep).coerceAtLeast(Duration.ZERO),
                awakeEvents = AwakeEvents(s.awakeCount, Duration.ofMinutes(s.totalAwakeMin.toLong())),
                medication = s.medication,
                dream = s.dreamType?.let { DreamRecall(it, s.dreamNote.ifBlank { null }) },
            )
            if (entryId != null) repo.update(entryId, protocol) else repo.save(protocol)
            _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }

    companion object {
        fun factory(app: Application, entryId: Long?) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                MorningViewModel(app, entryId) as T
        }
    }
}
