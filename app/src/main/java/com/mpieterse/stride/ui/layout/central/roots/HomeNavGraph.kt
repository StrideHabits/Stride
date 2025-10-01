package com.mpieterse.stride.ui.layout.central.roots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mpieterse.stride.ui.layout.central.views.HomeDatabaseScreen
import com.mpieterse.stride.ui.layout.central.views.HomeHabitViewerScreen
import com.mpieterse.stride.ui.layout.central.views.HomeSettingsScreen

@Composable
fun HomeNavGraph(
    modifier: Modifier = Modifier,
    controller: NavHostController,
    currentDestination: String
) {
    NavHost(
        navController = controller,
        startDestination = currentDestination
    ) {
        // Database
        composable(
            route = HomeScreen.Database.route
        ) {
            HomeDatabaseScreen(
                modifier = modifier
            )
        }

        // Settings
        composable(
            route = HomeScreen.Settings.route
        ) {
            HomeSettingsScreen(
                modifier = modifier
            )
        }

        // HabitViewer
        composable(
            route = HomeScreen.HabitViewer.route
        ) {
            HomeHabitViewerScreen(
                modifier = modifier
            )
        }
    }
}