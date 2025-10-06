package com.mpieterse.stride.ui.layout.startup.views

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onDismiss: () -> Unit,
    onFailure: () -> Unit,
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
        color = Color(0xFF_161620),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = 40.dp,
                        topEnd = 40.dp
                    )
                )
                .padding(32.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.60F)
            ) {
                Text(
                    text = stringResource(R.string.screen_auth_locked_page_heading),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.screen_auth_locked_page_content),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.40F)
            ) {
                IconButton(
                    onClick = {
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
                    },
                    modifier = Modifier
                        .requiredSize(56.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_qrcode_scan),
                        modifier = Modifier.requiredSize(56.dp),
                        contentDescription = stringResource(R.string.content_description_show_biometric_dialog),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}