package com.insomnia.diary.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class ThemeRepository private constructor(private val prefs: SharedPreferences) {
    private val _mode = MutableStateFlow(load())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun set(mode: ThemeMode) {
        _mode.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    private fun load(): ThemeMode =
        prefs.getString(KEY_THEME, null)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.DARK

    companion object {
        private const val PREFS_NAME = "insomnia_settings"
        private const val KEY_THEME = "theme_mode"

        @Volatile private var instance: ThemeRepository? = null

        fun getInstance(context: Context): ThemeRepository =
            instance ?: synchronized(this) {
                instance ?: ThemeRepository(
                    context.applicationContext
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                ).also { instance = it }
            }
    }
}
