package com.mpieterse.stride.ui.layout.startup.views

import androidx.biometric.BiometricPrompt
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

    when (val result = biometricResult) {
        is Final.Success -> onSuccess()
        else -> {
            // ...
        }
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
                
                // Biometric Icon (under the text)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable {
                            when (biometricService.isAvailable()) {
                                true -> {
                                    val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Login with biometrics")
                                        .setSubtitle("Authenticate to continue")
                                        .setNegativeButtonText("Cancel")

                                    biometricService.authenticate(activity, promptBuilder) { result ->
                                        biometricResult = result
                                    }
                                }

                                else -> {
                                    onSuccess()
                                }
                            }
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.fingerprint_24px),
                        modifier = Modifier.size(120.dp),
                        contentDescription = stringResource(R.string.content_description_show_biometric_dialog),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Helper text
                Text(
                    text = "Tap to authenticate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}