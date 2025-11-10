package com.mpieterse.stride.ui.layout.central.roots

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel
import com.mpieterse.stride.ui.layout.central.views.DebugScreen
import com.mpieterse.stride.ui.layout.central.views.HabitViewerScreen
import com.mpieterse.stride.ui.layout.central.views.HomeDatabaseScreen
import com.mpieterse.stride.ui.layout.central.views.HomeSettingsScreen
import com.mpieterse.stride.ui.layout.central.views.NotificationsScreen
import com.mpieterse.stride.ui.animations.fadeThroughEnter
import com.mpieterse.stride.ui.animations.fadeThroughExit
import com.mpieterse.stride.ui.animations.sharedAxisBackwardEnter
import com.mpieterse.stride.ui.animations.sharedAxisBackwardExit
import com.mpieterse.stride.ui.animations.sharedAxisForwardEnter
import com.mpieterse.stride.ui.animations.sharedAxisForwardExit
import com.mpieterse.stride.ui.animations.slideDownExit
import com.mpieterse.stride.ui.animations.slideUpEnter

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeNavGraph(
    modifier: Modifier = Modifier,
    controller: NavHostController,
    currentDestination: String,
    notificationsViewModel: NotificationsViewModel
) {
    AnimatedNavHost(
        navController = controller,
        startDestination = currentDestination,
        modifier = modifier,
        enterTransition = { fadeThroughEnter() },
        exitTransition = { fadeThroughExit() },
        popEnterTransition = { fadeThroughEnter() },
        popExitTransition = { fadeThroughExit() }
    ) {
        // Database
        composable(route = HomeScreen.Database.route) {
            HomeDatabaseScreen(
                modifier = Modifier,
                onNavigateToHabitViewer = { id ->
                    controller.navigate(HomeScreen.HabitViewer.buildRoute(id))
                }
            )
        }

        // Notifications
        composable(
            route = HomeScreen.Notifications.route
        ) {
            NotificationsScreen(
                modifier = Modifier,
                viewModel = notificationsViewModel
            )
        }

        // Settings
        composable(
            route = HomeScreen.Settings.route
        ) {
            HomeSettingsScreen(
                modifier = Modifier,
                onEnterDebug = {
                    controller.navigate(HomeScreen.Debug.route)
                }
            )
        }

        // HabitViewer
        composable(
            route = HomeScreen.HabitViewer.route,  // "habit/{habitId}"
            arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
            enterTransition = { slideUpEnter() },
            exitTransition = { sharedAxisForwardExit() },
            popEnterTransition = { sharedAxisBackwardEnter() },
            popExitTransition = { slideDownExit() }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitViewerScreen(
                modifier = Modifier,
                habitId = habitId,
                onBackClick = { controller.popBackStack() }
            )
        }

        // Debug
        composable(
            route = HomeScreen.Debug.route
        ) {
            DebugScreen(
                modifier = Modifier
            )
        }
    }
}