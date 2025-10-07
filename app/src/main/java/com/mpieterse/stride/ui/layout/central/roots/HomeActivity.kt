package com.mpieterse.stride.ui.layout.central.roots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.ui.layout.central.components.HomeScaffold
import com.mpieterse.stride.ui.layout.central.viewmodels.HomeSettingsViewModel
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel
import com.mpieterse.stride.ui.layout.shared.components.LocalStyledActivityStatusBar
import com.mpieterse.stride.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color(0xFF161620).toArgb()))

        setContent {
            val notificationsViewModel: NotificationsViewModel = hiltViewModel()
            val settingsViewModel: HomeSettingsViewModel = hiltViewModel()

            // collect theme
            val theme by settingsViewModel.theme.collectAsStateWithLifecycle()

            // ✅ reactively update via rememberUpdatedState
            val currentTheme by rememberUpdatedState(theme)

            // 👇 Wrap with key() to trigger recomposition when theme changes
            key(currentTheme) {
                StrideRoot(
                    theme = currentTheme,
                    notificationsViewModel = notificationsViewModel
                )
            }
        }
    }
}

@Composable
private fun StrideRoot(
    theme: AppAppearance,
    notificationsViewModel: NotificationsViewModel
) {
    val darkTheme = when (theme) {
        AppAppearance.LIGHT -> false
        AppAppearance.NIGHT -> true
        AppAppearance.SYSTEM -> isSystemInDarkTheme()
    }

    // Optional fade animation
    Crossfade(targetState = darkTheme, label = "ThemeSwitch") { dark ->
        AppTheme(darkTheme = dark) {
            HomeScaffold(notificationsViewModel = notificationsViewModel)

            LocalStyledActivityStatusBar(
                color = if (dark) Color(0xFF0E0E0E) else Color(0xFF161620)
            )
        }
    }
}
