package com.mpieterse.stride.ui.layout.central.roots

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel
import com.mpieterse.stride.ui.layout.central.views.DebugScreen
import com.mpieterse.stride.ui.layout.central.views.HabitViewerScreen
import com.mpieterse.stride.ui.layout.central.views.HomeDatabaseScreen
import com.mpieterse.stride.ui.layout.central.views.HomeSettingsScreen
import com.mpieterse.stride.ui.layout.central.views.NotificationsScreen

@Composable
fun HomeNavGraph(
    modifier: Modifier = Modifier,
    controller: NavHostController,
    currentDestination: String,
    notificationsViewModel: NotificationsViewModel
) {
    val fadeInSpec = tween<Float>(durationMillis = 220)
    val fadeOutSpec = tween<Float>(durationMillis = 180)

    NavHost(
        navController = controller,
        startDestination = currentDestination
    ) {
        // Database
        composable(
            route = HomeScreen.Database.route,
            enterTransition = { fadeIn(fadeInSpec) },
            exitTransition = { fadeOut(fadeOutSpec) },
            popEnterTransition = { fadeIn(fadeInSpec) },
            popExitTransition = { fadeOut(fadeOutSpec) }
        ) {
            HomeDatabaseScreen(
                modifier = modifier,
                onNavigateToHabitViewer = { id ->
                    controller.navigate(HomeScreen.HabitViewer.buildRoute(id))
                }
            )
        }

        // Notifications
        composable(
            route = HomeScreen.Notifications.route,
            enterTransition = { fadeIn(fadeInSpec) },
            exitTransition = { fadeOut(fadeOutSpec) },
            popEnterTransition = { fadeIn(fadeInSpec) },
            popExitTransition = { fadeOut(fadeOutSpec) }
        ) {
            NotificationsScreen(
                modifier = modifier,
                viewModel = notificationsViewModel
            )
        }

        // Settings
        composable(
            route = HomeScreen.Settings.route,
            enterTransition = { fadeIn(fadeInSpec) },
            exitTransition = { fadeOut(fadeOutSpec) },
            popEnterTransition = { fadeIn(fadeInSpec) },
            popExitTransition = { fadeOut(fadeOutSpec) }
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
            route = HomeScreen.HabitViewer.route,  // "habit/{habitId}"
            arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
            enterTransition = { fadeIn(fadeInSpec) },
            exitTransition = { fadeOut(fadeOutSpec) },
            popEnterTransition = { fadeIn(fadeInSpec) },
            popExitTransition = { fadeOut(fadeOutSpec) }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitViewerScreen(
                modifier = modifier,
                habitId = habitId,
                onBackClick = { controller.popBackStack() }
            )
        }

        // Debug
        composable(
            route = HomeScreen.Debug.route,
            enterTransition = { fadeIn(fadeInSpec) },
            exitTransition = { fadeOut(fadeOutSpec) },
            popEnterTransition = { fadeIn(fadeInSpec) },
            popExitTransition = { fadeOut(fadeOutSpec) }
        ) {
            DebugScreen(
                modifier = modifier
            )
        }
    }
}