package com.mpieterse.stride.ui.layout.startup.roots

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import com.mpieterse.stride.ui.layout.central.roots.HomeActivity
import com.mpieterse.stride.ui.layout.shared.components.LocalStyledActivityStatusBar
import com.mpieterse.stride.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    
    
// --- Lifecycle
    
    
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color(0xFF161620).toArgb()),
        )

        useAuthenticationStateRoutes()

        setContent {
            AppTheme {
                LocalStyledActivityStatusBar(
                    color = Color(0xFF161620)
                )

                Surface(
                    color = Color(0xFF161620),
                ) {
                    AuthNavGraph(
                        onGoToHomeActivity = ::navigateToHomeActivity,
                        onTerminateCompose = ::terminate,
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxSize()
                    )
                }
            }
        }
    }


// --- Internals


    private fun navigateToHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }


    private fun terminate() = finishAndRemoveTask()


    private fun useAuthenticationStateRoutes() {
        // ...
    }
}