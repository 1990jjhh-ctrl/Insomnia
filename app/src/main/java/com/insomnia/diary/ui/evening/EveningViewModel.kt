package com.insomnia.diary.ui.evening

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.repository.EveningRepository
import com.insomnia.diary.data.repository.MorningRepository
import com.insomnia.diary.domain.Attendee
import com.insomnia.diary.domain.DayEvent
import com.insomnia.diary.domain.EveningProtocol
import com.insomnia.diary.domain.EventType
import com.insomnia.diary.domain.EventTypePreset
import com.insomnia.diary.domain.Mood
import com.insomnia.diary.domain.Percentage
import com.insomnia.diary.domain.StressRange
import com.insomnia.diary.domain.Substance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val DEFAULT_EVENT_START_HOUR = 9
private const val DEFAULT_EVENT_END_HOUR = 10
private const val DEFAULT_STRESS_MIN = 20
private const val DEFAULT_STRESS_MAX = 40
private const val DEFAULT_BATTERY = 70

data class EventDraft(
    val typeLabel: String = EventTypePreset.WORK.label,
    val isCustom: Boolean = false,
    val start: LocalDateTime = LocalDate.now().atTime(DEFAULT_EVENT_START_HOUR, 0),
    val end: LocalDateTime = LocalDate.now().atTime(DEFAULT_EVENT_END_HOUR, 0),
    val stressMin: Int = DEFAULT_STRESS_MIN,
    val stressMax: Int = DEFAULT_STRESS_MAX,
    val batteryLevel: Int = DEFAULT_BATTERY,
    val attendeesText: String = "",
    val note: String = "",
)

data class EveningFormState(
    val moods: List<Mood> = emptyList(),
    val customMoods: List<Mood> = emptyList(),
    val productivity: Int = 70,
    val alcohol: List<Substance> = emptyList(),
    val events: List<EventDraft> = emptyList(),
    val customTypes: List<String> = emptyList(),
    val showEventSheet: Boolean = false,
    val editingEventIndex: Int? = null,
    val eventDraft: EventDraft = EventDraft(),
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val morningRecovery: Int? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
) {
    val date: LocalDate get() = recordedAt.toLocalDate()

    companion object {
        fun from(p: EveningProtocol) =
            EveningFormState(
                moods = p.moods,
                productivity = p.productivity.value,
                alcohol = p.alcohol,
                events =
                    p.events.map { e ->
                        EventDraft(
                            typeLabel = e.type.label,
                            isCustom = e.type.isCustom,
                            start = e.start,
                            end = e.end,
                            stressMin = e.stress.min.value,
                            stressMax = e.stress.max.value,
                            batteryLevel = e.batteryLevel.value,
                            attendeesText = e.attendees.joinToString(", ") { it.name },
                            note = e.note ?: "",
                        )
                    },
                recordedAt = p.recordedAt,
            )
    }
}

class EveningViewModel(app: Application, private val entryId: Long?) : AndroidViewModel(app) {
    val isEditing: Boolean get() = entryId != null
    private val repo = EveningRepository(InsomniaDatabase.getInstance(app))
    private val morningRepo = MorningRepository(InsomniaDatabase.getInstance(app))
    private val _state = MutableStateFlow(EveningFormState())
    val state: StateFlow<EveningFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            morningRepo.observeLatest().collect { morning ->
                _state.update { it.copy(morningRecovery = morning?.recovery?.value) }
            }
        }
        viewModelScope.launch {
            repo.observeCustomTypes().collect { types ->
                _state.update { it.copy(customTypes = types) }
            }
        }
        viewModelScope.launch {
            repo.observeCustomMoods().collect { moods ->
                _state.update { it.copy(customMoods = moods) }
            }
        }
        if (entryId != null) {
            viewModelScope.launch {
                repo.findById(entryId)?.let { p -> _state.update { EveningFormState.from(p) } }
            }
        }
    }

    fun toggleMood(mood: Mood) =
        _state.update { s ->
            val exists = s.moods.any { it.label == mood.label }
            s.copy(
                moods = if (exists) s.moods.filter { it.label != mood.label } else s.moods + mood,
            )
        }

    fun setDate(d: LocalDate) =
        _state.update { s ->
            val delta = ChronoUnit.DAYS.between(s.date, d)
            if (delta == 0L) return@update s
            s.copy(
                recordedAt = s.recordedAt.plusDays(delta),
                events =
                    s.events.map { e ->
                        e.copy(start = e.start.plusDays(delta), end = e.end.plusDays(delta))
                    },
            )
        }

    fun setProductivity(v: Int) = _state.update { it.copy(productivity = v) }

    fun addAlcohol(s: Substance) = _state.update { it.copy(alcohol = it.alcohol + s) }

    fun removeAlcohol(i: Int) =
        _state.update { s ->
            s.copy(alcohol = s.alcohol.toMutableList().also { it.removeAt(i) })
        }

    fun openNewEvent() =
        _state.update { s ->
            val prevBattery =
                s.events.lastOrNull()?.batteryLevel ?: s.morningRecovery ?: DEFAULT_BATTERY
            s.copy(
                showEventSheet = true,
                editingEventIndex = null,
                eventDraft = EventDraft(batteryLevel = prevBattery),
            )
        }

    fun openEditEvent(i: Int) =
        _state.update { s ->
            s.copy(showEventSheet = true, editingEventIndex = i, eventDraft = s.events[i])
        }

    fun updateEventDraft(d: EventDraft) = _state.update { it.copy(eventDraft = d) }

    fun closeEventSheet() = _state.update { it.copy(showEventSheet = false) }

    fun saveEventDraft() {
        val draft = _state.value.eventDraft
        _state.update { s ->
            val updated =
                if (s.editingEventIndex != null) {
                    s.events.toMutableList().also { it[s.editingEventIndex] = draft }
                } else {
                    s.events + draft
                }
            s.copy(events = updated, showEventSheet = false)
        }
        if (draft.isCustom && draft.typeLabel.isNotBlank()) {
            viewModelScope.launch { repo.saveCustomType(draft.typeLabel) }
        }
    }

    fun removeEvent(i: Int) =
        _state.update {
            it.copy(events = it.events.toMutableList().also { l -> l.removeAt(i) })
        }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val protocol =
                EveningProtocol(
                    recordedAt = if (entryId != null) s.recordedAt else LocalDateTime.now(),
                    moods = s.moods,
                    productivity = Percentage(s.productivity),
                    alcohol = s.alcohol,
                    events = s.events.map(EventDraft::toDomain),
                )
            if (entryId != null) repo.update(entryId, protocol) else repo.save(protocol)
            _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }

    companion object {
        fun factory(
            app: Application,
            entryId: Long?,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T = EveningViewModel(app, entryId) as T
        }
    }
}

private fun EventDraft.toDomain() =
    DayEvent(
        type = EventType(typeLabel, isCustom),
        start = start,
        end = end,
        stress = StressRange(Percentage(stressMin), Percentage(stressMax)),
        batteryLevel = Percentage(batteryLevel),
        attendees =
            attendeesText.split(",")
                .map { it.trim() }.filter { it.isNotBlank() }.map { Attendee(it) },
        note = note.ifBlank { null },
    )
