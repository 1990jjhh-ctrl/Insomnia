package com.insomnia.diary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.insomnia.diary.ui.AppNavigation
import com.insomnia.diary.ui.theme.InsomniaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InsomniaTheme {
                AppNavigation()
            }
        }
    }
}
