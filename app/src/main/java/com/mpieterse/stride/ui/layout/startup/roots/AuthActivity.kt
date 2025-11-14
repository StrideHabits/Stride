package com.mpieterse.stride.ui.layout.startup.roots

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.fragment.app.FragmentActivity
import com.mpieterse.stride.ui.layout.central.roots.HomeActivity
import com.mpieterse.stride.ui.layout.shared.components.LocalStyledActivityStatusBar
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
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
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.WHITE,
                darkScrim = android.graphics.Color.WHITE
            )
        )

        setContent {
            AppTheme {
                LocalStyledActivityStatusBar(color = MaterialTheme.colorScheme.background)
                val authState by authViewModel.authState.collectAsState()

                Surface(color = MaterialTheme.colorScheme.background) {
                    AnimatedContent(
                        targetState = authState,
                        transitionSpec = {
                            // Slide and fade when transitioning between auth states
                            val slideDirection = when {
                                targetState is AuthState.Locked && initialState is AuthState.Unauthenticated -> 1
                                initialState is AuthState.Locked && targetState is AuthState.Unauthenticated -> -1
                                else -> 0
                            }
                            
                            if (slideDirection != 0) {
                                slideInHorizontally(
                                    initialOffsetX = { it * slideDirection },
                                    animationSpec = TransitionConfig.slideInSpec
                                ) + fadeIn(TransitionConfig.fadeInSpec) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it * slideDirection },
                                    animationSpec = TransitionConfig.slideOutSpec
                                ) + fadeOut(TransitionConfig.fadeOutSpec)
                            } else {
                                fadeIn(TransitionConfig.fadeInSpec) togetherWith
                                fadeOut(TransitionConfig.fadeOutSpec)
                            }
                        },
                        label = "AuthStateTransition"
                    ) { state ->
                        when (state) {
                            is AuthState.Unauthenticated -> AuthNavGraph(
                                onGoToHomeActivity = ::navigateToHomeActivity,
                                modifier = Modifier.statusBarsPadding().fillMaxSize(),
                                authViewModel = authViewModel
                            )
                            is AuthState.Locked -> AuthLockedScreen(
                                modifier = Modifier.statusBarsPadding().fillMaxSize(),
                                onSuccess = {
                                    authViewModel.unlockWithBiometrics(true)
                                    navigateToHomeActivity()
                                },
                                model = authViewModel
                            )
                            is AuthState.Authenticated -> {
                                // Navigate immediately, no UI needed
                                navigateToHomeActivity()
                            }
                            // Don't navigate on Error - let screens handle it
                            is AuthState.Error -> AuthNavGraph(
                                onGoToHomeActivity = ::navigateToHomeActivity,
                                modifier = Modifier.statusBarsPadding().fillMaxSize(),
                                authViewModel = authViewModel
                            )
                        }
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