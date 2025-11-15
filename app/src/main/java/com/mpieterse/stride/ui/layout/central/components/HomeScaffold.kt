package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.roots.HomeNavGraph
import com.mpieterse.stride.ui.layout.central.roots.HomeScreen
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel

@Composable
fun HomeScaffold(notificationsViewModel: NotificationsViewModel) { //This composable creates the main app scaffold with bottom navigation using Jetpack Compose (Android Developers, 2024).
    val controller = rememberNavController()
    val destinationDefault = HomeScreen.Database
    
    // Use back stack entry to track current destination for better state management
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: destinationDefault.route
    var destinationCurrent by rememberSaveable { mutableStateOf(currentRoute) }
    
    // Update destinationCurrent when route changes from back stack
    LaunchedEffect(currentRoute) {
        if (currentRoute in listOf(HomeScreen.Database.route, HomeScreen.Notifications.route, HomeScreen.Settings.route)) {
            destinationCurrent = currentRoute
        }
    }

    val destinations = listOf(
        BottomNavItem(
            label = "Home",
            alias = HomeScreen.Database,
            icon = painterResource(R.drawable.xic_uic_outline_check_circle)
        ),
        BottomNavItem(
            label = "Notifications",
            alias = HomeScreen.Notifications,
            icon = painterResource(R.drawable.xic_uic_outline_bell)
        ),
        BottomNavItem(
            label = "Settings",
            alias = HomeScreen.Settings,
            icon = painterResource(R.drawable.xic_uic_outline_setting)
        ),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface,
                windowInsets = NavigationBarDefaults.windowInsets,
                modifier = Modifier.shadow(8.dp)
            ) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = (destinationCurrent == destination.alias.route),
                        icon = {
                            Icon(
                                painter = destination.icon,
                                contentDescription = destination.contentDescription,
                            )
                        },
                        onClick = {
                            val targetRoute = destination.alias.route
                            if (destinationCurrent != targetRoute) {
                                destinationCurrent = targetRoute
                                // Use popUpTo to maintain clean back stack, but disable restoreState
                                // to ensure transitions always play
                                controller.navigate(targetRoute) {
                                    // Pop up to start destination but keep state saving
                                    popUpTo(controller.graph.startDestinationId) {
                                        saveState = true
                                        inclusive = false  // Don't pop the start destination itself
                                    }
                                    // Don't restore state to allow transitions to always play
                                    launchSingleTop = true
                                    restoreState = false  // Disable to ensure transitions work
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { insets ->
        Surface(color = Color(0xFF161620)) {
            HomeNavGraph(
                controller = controller,
                currentDestination = destinationCurrent,
                modifier = Modifier
                    .padding(insets)
                    .fillMaxSize(),
                notificationsViewModel = notificationsViewModel
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val alias: HomeScreen,
    val icon: Painter,
    val contentDescription: String? = null
)
