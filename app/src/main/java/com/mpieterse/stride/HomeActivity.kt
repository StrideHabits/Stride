package com.mpieterse.stride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpieterse.stride.ui.theme.AppTheme
import com.mpieterse.stride.login.LoginScreen
import com.mpieterse.stride.login.SignupScreen
import com.mpieterse.stride.lock.LockScreen
import com.mpieterse.stride.home.HomeScreen
import com.mpieterse.stride.settings.SettingsScreen
import com.mpieterse.stride.viewer.HabitViewerScreen

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { 
                            LoginScreen(
                                onContinue = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                                onRequestSignup = { navController.navigate("signup") }
                            ) 
                        }
                        composable("signup") {
                            SignupScreen(
                                onCreateAccount = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                                onLoginLink = { navController.popBackStack(); }
                            )
                        }
                        composable("lock") {
                            LockScreen(onBiometricTap = { /* TODO(auth): launch BiometricPrompt */ navController.navigate("home") { popUpTo("login") { inclusive = true } } })
                        }
                        composable("home") {
                            HomeScreen(
                                onCreateHabit = { /* TODO: open upsert already wired */ },
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenHabit = { id -> navController.navigate("habit/$id") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onImport = { /* TODO(data): import */ },
                                onExport = { /* TODO(data): export */ },
                                onHelp = { /* TODO(ui): open help */ },
                                onLogout = { /* TODO(auth): logout */ }
                            )
                        }
                        composable("habit/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            HabitViewerScreen(habitId = id, onEdit = { /* TODO(nav): open upsert prefilled */ })
                        }
                    }
                }
            }
        }
    }
}