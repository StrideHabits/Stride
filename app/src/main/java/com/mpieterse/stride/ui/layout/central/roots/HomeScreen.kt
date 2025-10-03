package com.mpieterse.stride.ui.layout.central.roots

/**
 * Logical alias for [HomeActivity] screen routes.
 */
sealed class HomeScreen(
    val route: String
) {
    object Database : HomeScreen("database")
    object Notifications : HomeScreen("notifications")
    object Settings : HomeScreen("settings")
    object HabitViewer : HomeScreen("habitViewer")
    object Debug : HomeScreen("debug")
}