package com.mpieterse.stride.ui.layout.startup.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedTextField
import com.mpieterse.stride.ui.layout.shared.components.TextFieldType
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel

@Composable
fun AuthSignUpScreen(
    onSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier,
    viewModel: AuthViewModel
) {
    val analytics = Firebase.analytics
    LaunchedEffect(Unit) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "SignUp")
        }
    }

    val formState by viewModel.signUpForm.formState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

// --- UI

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
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
            Text(
                text = stringResource(R.string.auth_sign_up_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )

            Spacer(
                Modifier.height(32.dp)
            )
            
            // Error Message
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
                exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
            ) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(R.drawable.xic_uic_outline_user_plus),
                        contentDescription = "",
                        modifier = Modifier.requiredSize(16.dp)
                    )
                    Spacer(
                        Modifier.width(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.auth_sign_up_account_section),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    )
                }
                Spacer(
                    Modifier.height(12.dp)
                )
                LocalOutlinedTextField(
                    label = stringResource(R.string.auth_sign_up_email_label),
                    value = formState.identity.value,
                    onValueChange = { value ->
                        viewModel.signUpForm.onIdentityChanged(value)
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    inputType = KeyboardType.Email,
                    fieldType = TextFieldType.Default,
                    inputAction = ImeAction.Next,
                    isComponentErrored = (formState.identity.error != null),
                    isComponentEnabled = !isLoading
                )
            }

            Spacer(
                Modifier.height(64.dp)
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(R.drawable.xic_uic_outline_shield),
                        contentDescription = "",
                        modifier = Modifier.requiredSize(16.dp)
                    )
                    Spacer(
                        Modifier.width(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.auth_sign_up_security_section),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    )
                }

                Spacer(
                    Modifier.height(12.dp)
                )
                LocalOutlinedTextField(
                    label = stringResource(R.string.auth_sign_up_password_label),
                    value = formState.passwordDefault.value,
                    onValueChange = { value ->
                        viewModel.signUpForm.onPasswordDefaultChanged(value)
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Private,
                    inputAction = ImeAction.Next,
                    isComponentErrored = (formState.passwordDefault.error != null),
                    isComponentEnabled = !isLoading
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                LocalOutlinedTextField(
                    label = stringResource(R.string.auth_sign_up_password_confirm_label),
                    value = formState.passwordConfirm.value,
                    onValueChange = { value ->
                        viewModel.signUpForm.onPasswordConfirmChanged(value)
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Private,
                    inputAction = ImeAction.Done,
                    isComponentErrored = (formState.passwordConfirm.error != null),
                    isComponentEnabled = !isLoading
                )
            }

            Spacer(
                Modifier.height(32.dp)
            )
            Column {
                Button(
                    onClick = {
                        onSignUp()
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(52.dp)
                        .fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.auth_sign_up_button),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight(600)
                            )
                        )
                    }
                }

                Spacer(
                    Modifier.height(24.dp)
                )
                Text(
                    text = stringResource(R.string.auth_sign_up_have_account),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(enabled = !isLoading) {
                            onNavigateToSignIn()
                        }
                        .padding(
                            horizontal = 6.dp,
                            vertical = 4.dp
                        )
                        .align(Alignment.CenterHorizontally),
                )

                Spacer(
                    Modifier.height(12.dp)
                )
                Text(
                    text = stringResource(R.string.auth_sign_up_help_faq),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { /* ... */ }
                        .padding(
                            horizontal = 6.dp,
                            vertical = 4.dp
                        )
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}