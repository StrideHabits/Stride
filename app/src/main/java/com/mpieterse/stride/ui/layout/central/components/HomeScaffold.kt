package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.roots.HomeNavGraph
import com.mpieterse.stride.ui.layout.central.roots.HomeScreen

@Composable
fun HomeScaffold() {
    val controller = rememberNavController()
    val destinationDefault = HomeScreen.Database
    var destinationCurrent by rememberSaveable {
        mutableStateOf(destinationDefault.route)
    }
    var showCreateHabitDialog by rememberSaveable { mutableStateOf(false) }
    
    // Get the current destination from the navigation controller
    val currentRoute = controller.currentBackStackEntryAsState().value?.destination?.route

    val destinations = listOf(
        BottomNavItem(
            label = "Home",
            alias = HomeScreen.Database,
            icon = painterResource(
                R.drawable.xic_uic_outline_check_circle
            )
        ),
        BottomNavItem(
            label = "Notifications",
            alias = HomeScreen.Notifications,
            icon = painterResource(
                R.drawable.xic_uic_outline_bell
            )
        ),
        BottomNavItem(
            label = "Settings",
            alias = HomeScreen.Settings,
            icon = painterResource(
                R.drawable.xic_uic_outline_setting
            )
        ),
    )

    Scaffold(
        floatingActionButton = {
            // Only show FAB on Database screen, not on HabitViewer
            if (currentRoute == HomeScreen.Database.route) {
                FloatingActionButton(
                    onClick = { showCreateHabitDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_plus),
                        contentDescription = "Add Habit"
                    )
                }
            }
        },
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
                            controller.navigate(destination.alias.route)
                            destinationCurrent = destination.alias.route
                        }
                    )
                }
            }
        }
    ) { insets ->
        Surface(
            color = Color(0xFF161620),
        ) {
            HomeNavGraph(
                controller = controller,
                currentDestination = destinationCurrent,
                modifier = Modifier
                    .padding(insets)
                    .fillMaxSize()
            )
        }
    }

    // Create Habit Dialog
    UpsertDialog(
        isVisible = showCreateHabitDialog,
        onDismiss = { showCreateHabitDialog = false },
        onConfirm = { habitData ->
            // TODO: Handle habit creation
            // For now, just log the data
            println("Created habit: ${habitData.name}, frequency: ${habitData.frequency}, tag: ${habitData.tag}")
        }
    )
}


data class BottomNavItem(
    val label: String,
    val alias: HomeScreen,
    val icon: Painter,
    val contentDescription: String? = null
)