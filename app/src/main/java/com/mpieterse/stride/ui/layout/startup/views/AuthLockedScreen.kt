package com.mpieterse.stride.ui.layout.startup.views

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.mpieterse.stride.R
import com.mpieterse.stride.core.models.results.BiometricError
import com.mpieterse.stride.core.models.results.Final
import com.mpieterse.stride.core.services.BiometricService
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthLockedScreen(
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit,
    model: AuthViewModel = hiltViewModel()
) {
    val analytics = Firebase.analytics
    LaunchedEffect(Unit) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Lockscreen")
        }
    }

    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometricService = BiometricService(context)
    var biometricResult by remember {
        mutableStateOf<Final<Unit, BiometricError>?>(null)
    }
    
    // Check if Firebase auth is still active - if not, redirect to login
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Auth session is no longer active, redirect to login
            model.unlockWithAlternativeMethod()
        }
    }

    // Handle success case separately to avoid side effects in computed value
    LaunchedEffect(biometricResult) {
        if (biometricResult is Final.Success) {
            onSuccess()
        }
    }
    
    // Derive error message directly from biometricResult to avoid stale state
    val errorMessage = when (val result = biometricResult) {
        is Final.Success -> null
        is Final.Failure -> when (result.problem) {
            is BiometricError.Dismissed -> null // User cancelled, don't show error
            is BiometricError.Failed -> "Authentication failed. Please try again."
            is BiometricError.NoSupport -> context.getString(R.string.auth_biometric_unavailable_error)
            is BiometricError.RateLimit -> "Too many failed attempts. Please try again later."
            is BiometricError.Exception -> "System error. Please try again."
        }
        null -> null // No result yet
    }
    
    // Check if biometrics are unavailable (either from service check or from error)
    val biometricsUnavailable = when {
        !biometricService.isAvailable() -> true
        biometricResult is Final.Failure -> {
            val failure = biometricResult as Final.Failure
            failure.problem is BiometricError.NoSupport
        }
        else -> false
    }

// --- UI

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                // Welcome Message / Heading
                Text(
                    text = stringResource(R.string.screen_auth_locked_page_heading),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Subtitle / Content
                Text(
                    text = stringResource(R.string.screen_auth_locked_page_content),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(64.dp))
                
                // Error Message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
                    exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
                ) {
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Biometric Icon (lower, under the text)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable(enabled = biometricService.isAvailable()) {
                            // Reset result when retrying to clear previous error
                            biometricResult = null
                            
                            if (biometricService.isAvailable()) {
                                val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(context.getString(R.string.auth_biometric_prompt_title))
                                    .setSubtitle(context.getString(R.string.auth_biometric_prompt_subtitle))
                                    .setNegativeButtonText(context.getString(R.string.auth_biometric_prompt_cancel))

                                biometricService.authenticate(activity, promptBuilder) { result ->
                                    biometricResult = result
                                }
                            }
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.fingerprint_24px),
                        modifier = Modifier.size(120.dp),
                        contentDescription = stringResource(R.string.content_description_show_biometric_dialog),
                        tint = if (biometricService.isAvailable()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Helper text
                Text(
                    text = stringResource(R.string.auth_locked_tap_to_authenticate),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Fallback unlock button when biometrics are unavailable
                AnimatedVisibility(
                    visible = biometricsUnavailable,
                    enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
                    exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                model.unlockWithAlternativeMethod()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.auth_locked_use_email_password),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}