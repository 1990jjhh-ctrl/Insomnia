package com.insomnia.diary.ui.evening

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.repository.EveningRepository
import com.insomnia.diary.domain.Attendee
import com.insomnia.diary.domain.BatteryCheckpoint
import com.insomnia.diary.domain.DayEvent
import com.insomnia.diary.domain.EventType
import com.insomnia.diary.domain.EventTypePreset
import com.insomnia.diary.domain.EveningProtocol
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

data class EventDraft(
    val typeLabel: String = EventTypePreset.WORK.label,
    val isCustom: Boolean = false,
    val start: LocalDateTime = LocalDate.now().atTime(9, 0),
    val end: LocalDateTime = LocalDate.now().atTime(10, 0),
    val stressMin: Int = 20,
    val stressMax: Int = 40,
    val attendeesText: String = "",
    val note: String = "",
)

data class BatteryDraft(
    val at: LocalDateTime = LocalDateTime.now(),
    val level: Int = 70,
)

data class EveningFormState(
    val moods: List<Mood> = emptyList(),
    val productivity: Int = 70,
    val alcohol: List<Substance> = emptyList(),
    val events: List<EventDraft> = emptyList(),
    val battery: List<BatteryDraft> = emptyList(),
    val showEventSheet: Boolean = false,
    val editingEventIndex: Int? = null,
    val eventDraft: EventDraft = EventDraft(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
)

class EveningViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = EveningRepository(InsomniaDatabase.getInstance(app))
    private val _state = MutableStateFlow(EveningFormState())
    val state: StateFlow<EveningFormState> = _state.asStateFlow()

    fun toggleMood(mood: Mood) = _state.update { s ->
        val exists = s.moods.any { it.label == mood.label }
        s.copy(moods = if (exists) s.moods.filter { it.label != mood.label } else s.moods + mood)
    }

    fun setProductivity(v: Int) = _state.update { it.copy(productivity = v) }

    fun addAlcohol(s: Substance) = _state.update { it.copy(alcohol = it.alcohol + s) }
    fun removeAlcohol(i: Int) = _state.update { it.copy(alcohol = it.alcohol.toMutableList().also { l -> l.removeAt(i) }) }

    fun openNewEvent() = _state.update { it.copy(showEventSheet = true, editingEventIndex = null, eventDraft = EventDraft()) }
    fun openEditEvent(i: Int) = _state.update { it.copy(showEventSheet = true, editingEventIndex = i, eventDraft = it.events[i]) }
    fun updateEventDraft(d: EventDraft) = _state.update { it.copy(eventDraft = d) }
    fun closeEventSheet() = _state.update { it.copy(showEventSheet = false) }

    fun saveEventDraft() = _state.update { s ->
        val updated = if (s.editingEventIndex != null) {
            s.events.toMutableList().also { it[s.editingEventIndex] = s.eventDraft }
        } else {
            s.events + s.eventDraft
        }
        s.copy(events = updated, showEventSheet = false)
    }

    fun removeEvent(i: Int) = _state.update { it.copy(events = it.events.toMutableList().also { l -> l.removeAt(i) }) }

    fun addBattery() = _state.update { it.copy(battery = it.battery + BatteryDraft(at = LocalDateTime.now())) }
    fun updateBattery(i: Int, d: BatteryDraft) = _state.update { it.copy(battery = it.battery.toMutableList().also { l -> l[i] = d }) }
    fun removeBattery(i: Int) = _state.update { it.copy(battery = it.battery.toMutableList().also { l -> l.removeAt(i) }) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.save(EveningProtocol(
                recordedAt = LocalDateTime.now(),
                moods = s.moods,
                productivity = Percentage(s.productivity),
                alcohol = s.alcohol,
                events = s.events.map(EventDraft::toDomain),
                battery = s.battery.map { BatteryCheckpoint(it.at, Percentage(it.level)) },
            ))
            _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}

private fun EventDraft.toDomain() = DayEvent(
    type = EventType(typeLabel, isCustom),
    start = start,
    end = end,
    stress = StressRange(Percentage(stressMin), Percentage(stressMax)),
    attendees = attendeesText.split(",")
        .map { it.trim() }.filter { it.isNotBlank() }.map { Attendee(it) },
    note = note.ifBlank { null },
)
