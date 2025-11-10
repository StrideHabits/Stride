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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import com.mpieterse.stride.ui.layout.central.roots.HomeActivity
import com.mpieterse.stride.ui.layout.shared.components.LocalStyledActivityStatusBar
import com.mpieterse.stride.ui.layout.startup.models.AuthState
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel
import com.mpieterse.stride.ui.layout.startup.views.AuthLockedScreen
import com.mpieterse.stride.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color(0xFF161620).toArgb())
        )

        setContent {
            AppTheme {
                LocalStyledActivityStatusBar(color = Color(0xFF161620))
                val authState by authViewModel.authState.collectAsState()

                Surface(color = Color(0xFF161620)) {
                    when (authState) {
                        is AuthState.Unauthenticated,
                        is AuthState.Error,
                        is AuthState.Loading -> {
                            // Keep showing AuthNavGraph for these states to preserve navigation
                            // The individual screens (SignIn/SignUp) will handle loading/error states
                            // By grouping these states together, AuthNavGraph won't be recreated
                            // when authState changes from Unauthenticated to Loading/Error
                            AuthNavGraph(
                                onGoToHomeActivity = ::navigateToHomeActivity,
                                modifier = Modifier.statusBarsPadding().fillMaxSize(),
                                authViewModel = authViewModel
                            )
                        }
                        is AuthState.Locked -> AuthLockedScreen(
                            modifier = Modifier.statusBarsPadding().fillMaxSize(),
                            onSuccess = {
                                authViewModel.unlockWithBiometrics(true)
                                navigateToHomeActivity()
                            },
                            model = authViewModel
                        )
                        is AuthState.Authenticated -> navigateToHomeActivity()
                    }
                }
            }
        }
    }


// --- Internals


    private fun navigateToHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }
}