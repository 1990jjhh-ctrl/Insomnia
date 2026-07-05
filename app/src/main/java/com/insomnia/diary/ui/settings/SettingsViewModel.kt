package com.insomnia.diary.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.preferences.ThemeMode
import com.insomnia.diary.data.preferences.ThemeRepository
import com.insomnia.diary.data.repository.EveningRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val FLOW_TIMEOUT_MS = 5_000L

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val themeRepo = ThemeRepository.getInstance(app)
    private val eveningRepo = EveningRepository(InsomniaDatabase.getInstance(app))

    val themeMode: StateFlow<ThemeMode> = themeRepo.mode

    val customTypes: StateFlow<List<String>> =
        eveningRepo.observeCustomTypes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MS), emptyList())

    val customMoods: StateFlow<List<String>> =
        eveningRepo.observeCustomMoods()
            .map { list -> list.map { it.label } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MS), emptyList())

    fun setThemeMode(mode: ThemeMode) = themeRepo.set(mode)

    fun addCustomType(label: String) = viewModelScope.launch { eveningRepo.saveCustomType(label) }

    fun deleteCustomType(label: String) =
        viewModelScope.launch {
            eveningRepo.deleteCustomType(
                label,
            )
        }

    fun addCustomMood(label: String) = viewModelScope.launch { eveningRepo.saveCustomMood(label) }

    fun deleteCustomMood(label: String) =
        viewModelScope.launch {
            eveningRepo.deleteCustomMood(
                label,
            )
        }
}
