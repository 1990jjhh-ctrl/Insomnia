package com.insomnia.diary.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.insomnia.diary.ui.evening.EveningScreen
import com.insomnia.diary.ui.evening.EveningViewModel
import com.insomnia.diary.ui.home.HomeScreen
import com.insomnia.diary.ui.morning.MorningScreen
import com.insomnia.diary.ui.morning.MorningViewModel
import com.insomnia.diary.ui.settings.SettingsScreen
import com.insomnia.diary.ui.settings.SettingsViewModel

private const val NO_ID = -1L

object Destination {
    const val HOME = "home"
    const val SETTINGS = "settings"

    object Morning {
        const val ROUTE = "morning?id={id}"
        const val NEW = "morning?id=$NO_ID"
        val ARGS = listOf(navArgument("id") { type = NavType.LongType; defaultValue = NO_ID })
        fun edit(id: Long) = "morning?id=$id"
    }

    object Evening {
        const val ROUTE = "evening?id={id}"
        const val NEW = "evening?id=$NO_ID"
        val ARGS = listOf(navArgument("id") { type = NavType.LongType; defaultValue = NO_ID })
        fun edit(id: Long) = "evening?id=$id"
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val app = LocalContext.current.applicationContext as Application

    NavHost(navController = nav, startDestination = Destination.HOME) {
        composable(Destination.HOME) {
            HomeScreen(
                onStartMorning = { nav.navigate(Destination.Morning.NEW) },
                onStartEvening = { nav.navigate(Destination.Evening.NEW) },
                onEditMorning = { id -> nav.navigate(Destination.Morning.edit(id)) },
                onEditEvening = { id -> nav.navigate(Destination.Evening.edit(id)) },
                onSettings = { nav.navigate(Destination.SETTINGS) },
            )
        }
        composable(Destination.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                viewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>,
                        extras: androidx.lifecycle.viewmodel.CreationExtras,
                    ): T = SettingsViewModel(app) as T
                }),
            )
        }
        composable(Destination.Morning.ROUTE, Destination.Morning.ARGS) { back ->
            val id = back.arguments?.getLong("id")?.takeIf { it != NO_ID }
            MorningScreen(
                onDone = { nav.popBackStack() },
                viewModel = viewModel(factory = MorningViewModel.factory(app, id)),
            )
        }
        composable(Destination.Evening.ROUTE, Destination.Evening.ARGS) { back ->
            val id = back.arguments?.getLong("id")?.takeIf { it != NO_ID }
            EveningScreen(
                onDone = { nav.popBackStack() },
                viewModel = viewModel(factory = EveningViewModel.factory(app, id)),
            )
        }
    }
}
