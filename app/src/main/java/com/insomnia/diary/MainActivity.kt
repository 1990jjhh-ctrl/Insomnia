package com.insomnia.diary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.insomnia.diary.data.preferences.ThemeRepository
import com.insomnia.diary.ui.AppNavigation
import com.insomnia.diary.ui.theme.InsomniaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeRepo = ThemeRepository.getInstance(this)
        setContent {
            val themeMode by themeRepo.mode.collectAsStateWithLifecycle()
            InsomniaTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}
