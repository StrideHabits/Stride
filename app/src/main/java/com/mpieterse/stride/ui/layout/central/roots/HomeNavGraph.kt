package com.mpieterse.stride.ui.layout.central.roots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mpieterse.stride.ui.layout.central.views.DebugScreen
import com.mpieterse.stride.ui.layout.central.views.HabitViewerScreen
import com.mpieterse.stride.ui.layout.central.views.HomeDatabaseScreen
import com.mpieterse.stride.ui.layout.central.views.HomeSettingsScreen
import com.mpieterse.stride.ui.layout.central.views.NotificationsScreen

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
                modifier = modifier,
                onNavigateToHabitViewer = {
                    controller.navigate(HomeScreen.HabitViewer.route)
                }
            )
        }

        // Notifications
        composable(
            route = HomeScreen.Notifications.route
        ) {
            NotificationsScreen(
                modifier = modifier
            )
        }

        // Settings
        composable(
            route = HomeScreen.Settings.route
        ) {
            HomeSettingsScreen(
                modifier = modifier,
                onEnterDebug = {
                    controller.navigate(HomeScreen.Debug.route)
                }
            )
        }

        // HabitViewer
        composable(
            route = HomeScreen.HabitViewer.route
        ) {
            HabitViewerScreen(
                modifier = modifier,
                onBackClick = {
                    controller.popBackStack()
                }
            )
        }

        // Debug
        composable(
            route = HomeScreen.Debug.route
        ) {
            DebugScreen(
                modifier = modifier
            )
        }
    }
}