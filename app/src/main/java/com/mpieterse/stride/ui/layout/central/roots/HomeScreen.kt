package com.mpieterse.stride.ui.layout.central.roots

/**
 * Logical alias for [HomeActivity] screen routes.
 */

sealed class HomeScreen(val route: String) {
    object Database      : HomeScreen("database")
    object Notifications : HomeScreen("notifications")
    object Settings      : HomeScreen("settings")

    // pattern with argument
    object HabitViewer   : HomeScreen("habit/{habitId}") {
        fun buildRoute(habitId: String) = "habit/$habitId"
    }

    object Debug         : HomeScreen("debug")
}
