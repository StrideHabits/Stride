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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.roots.HomeNavGraph
import com.mpieterse.stride.ui.layout.central.roots.HomeScreen
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel

@Composable
fun HomeScaffold(notificationsViewModel: NotificationsViewModel) {
    val controller = rememberNavController()
    val destinationDefault = HomeScreen.Database
    var destinationCurrent by rememberSaveable { mutableStateOf(destinationDefault.route) }

    val currentRoute = controller.currentBackStackEntryAsState().value?.destination?.route

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
                            controller.navigate(destination.alias.route) {
                                popUpTo(HomeScreen.Database.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            destinationCurrent = destination.alias.route
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
