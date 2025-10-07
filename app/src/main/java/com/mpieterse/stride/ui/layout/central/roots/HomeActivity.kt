package com.mpieterse.stride.ui.layout.central.roots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mpieterse.stride.ui.layout.central.components.HomeScaffold
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel
import com.mpieterse.stride.ui.layout.shared.components.LocalStyledActivityStatusBar
import com.mpieterse.stride.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val notificationsViewModel: NotificationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color(0xFF161620).toArgb()),
        )

        setContent {
            AppTheme {
                HomeScaffold(
                    notificationsViewModel = notificationsViewModel
                )

                LocalStyledActivityStatusBar(
                    color = Color(0xFF161620)
                )
            }
        }
    }
}
