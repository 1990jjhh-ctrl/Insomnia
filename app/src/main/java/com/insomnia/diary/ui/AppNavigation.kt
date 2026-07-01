package com.insomnia.diary.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.insomnia.diary.ui.evening.EveningScreen
import com.insomnia.diary.ui.home.HomeScreen
import com.insomnia.diary.ui.morning.MorningScreen

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Morning : Destination("morning")
    data object Evening : Destination("evening")
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Destination.Home.route) {
        composable(Destination.Home.route) {
            HomeScreen(
                onStartMorning = { nav.navigate(Destination.Morning.route) },
                onStartEvening = { nav.navigate(Destination.Evening.route) },
            )
        }
        composable(Destination.Morning.route) {
            MorningScreen(onDone = { nav.popBackStack() })
        }
        composable(Destination.Evening.route) {
            EveningScreen(onDone = { nav.popBackStack() })
        }
    }
}
