package com.microhabits.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.microhabits.ui.screens.FocusScreen
import com.microhabits.ui.screens.HomeScreen
import com.microhabits.ui.screens.StatsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val HOME  = "home"
    const val STATS = "stats"
    const val FOCUS = "focus/{habitId}/{habitName}/{habitEmoji}/{durationMinutes}"

    fun focusRoute(id: Int, name: String, emoji: String, duration: Int): String {
        val encodedName  = URLEncoder.encode(name,  StandardCharsets.UTF_8.toString())
        val encodedEmoji = URLEncoder.encode(emoji, StandardCharsets.UTF_8.toString())
        return "focus/$id/$encodedName/$encodedEmoji/$duration"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToFocus = { id, name, emoji, duration ->
                    navController.navigate(Routes.focusRoute(id, name, emoji, duration))
                },
                onNavigateToStats = { navController.navigate(Routes.STATS) }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.FOCUS,
            arguments = listOf(
                navArgument("habitId")        { type = NavType.IntType },
                navArgument("habitName")      { type = NavType.StringType },
                navArgument("habitEmoji")     { type = NavType.StringType },
                navArgument("durationMinutes"){ type = NavType.IntType }
            )
        ) { backStack ->
            val id       = backStack.arguments!!.getInt("habitId")
            val name     = URLDecoder.decode(backStack.arguments!!.getString("habitName")!!,  "UTF-8")
            val emoji    = URLDecoder.decode(backStack.arguments!!.getString("habitEmoji")!!, "UTF-8")
            val duration = backStack.arguments!!.getInt("durationMinutes")
            FocusScreen(
                habitId = id, habitName = name, habitEmoji = emoji, durationMinutes = duration,
                onFinish = { navController.popBackStack() }
            )
        }
    }
}
