package com.mpieterse.stride.ui.layout.central.roots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mpieterse.stride.ui.layout.central.viewmodels.HomeSettingsViewModel
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel
import com.mpieterse.stride.ui.layout.central.views.DebugScreen
import com.mpieterse.stride.ui.layout.central.views.HabitViewerScreen
import com.mpieterse.stride.ui.layout.central.views.HomeDatabaseScreen
import com.mpieterse.stride.ui.layout.central.views.HomeSettingsScreen
import com.mpieterse.stride.ui.layout.central.views.NotificationsScreen
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig

@Composable
fun HomeNavGraph(
    modifier: Modifier = Modifier,
    controller: NavHostController,
    currentDestination: String,
    notificationsViewModel: NotificationsViewModel,
    homeSettingsViewModel: HomeSettingsViewModel
) {
    NavHost(
        navController = controller,
        startDestination = currentDestination,
        // Remove startDestination parameter usage to prevent transition issues
    ) {
        // Database - Tab navigation uses fade
        composable(
            route = HomeScreen.Database.route,
            enterTransition = { TransitionConfig.horizontalFadeTransition() },
            exitTransition = { TransitionConfig.horizontalFadeExit() },
            popEnterTransition = { TransitionConfig.horizontalFadeTransition() },
            popExitTransition = { TransitionConfig.horizontalFadeExit() }
        ) {
            HomeDatabaseScreen(
                modifier = modifier,
                onNavigateToHabitViewer = { id ->
                    controller.navigate(HomeScreen.HabitViewer.buildRoute(id))
                }
            )
        }

        // Notifications - Tab navigation uses fade
        composable(
            route = HomeScreen.Notifications.route,
            enterTransition = { TransitionConfig.horizontalFadeTransition() },
            exitTransition = { TransitionConfig.horizontalFadeExit() },
            popEnterTransition = { TransitionConfig.horizontalFadeTransition() },
            popExitTransition = { TransitionConfig.horizontalFadeExit() }
        ) {
            NotificationsScreen(
                modifier = modifier,
                viewModel = notificationsViewModel
            )
        }

        // Settings - Tab navigation uses fade
        composable(
            route = HomeScreen.Settings.route,
            enterTransition = { TransitionConfig.horizontalFadeTransition() },
            exitTransition = { TransitionConfig.horizontalFadeExit() },
            popEnterTransition = { TransitionConfig.horizontalFadeTransition() },
            popExitTransition = { TransitionConfig.horizontalFadeExit() }
        ) {
            HomeSettingsScreen(
                modifier = modifier,
                viewModel = homeSettingsViewModel,
                notificationsViewModel = notificationsViewModel,
                onEnterDebug = {
                    controller.navigate(HomeScreen.Debug.route)
                }
            )
        }

        // HabitViewer - Detail view uses slide transition
        composable(
            route = HomeScreen.HabitViewer.route,  // "habit/{habitId}"
            arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
            enterTransition = { TransitionConfig.forwardSlideTransition() },
            exitTransition = { TransitionConfig.forwardSlideExit() },
            popEnterTransition = { TransitionConfig.backwardSlideTransition() },
            popExitTransition = { TransitionConfig.backwardSlideExit() }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitViewerScreen(
                modifier = modifier,
                habitId = habitId,
                onBackClick = { controller.popBackStack() }
            )
        }

        // Debug - Modal-like screen uses vertical slide
        composable(
            route = HomeScreen.Debug.route,
            enterTransition = { TransitionConfig.verticalSlideTransition() },
            exitTransition = { TransitionConfig.verticalSlideExit() },
            popEnterTransition = { TransitionConfig.verticalSlideTransition() },
            popExitTransition = { TransitionConfig.verticalSlideExit() }
        ) {
            DebugScreen(
                modifier = modifier
            )
        }
    }
}