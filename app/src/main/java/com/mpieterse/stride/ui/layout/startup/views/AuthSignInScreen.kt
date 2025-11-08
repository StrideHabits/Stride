package com.mpieterse.stride.ui.layout.startup.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.Firebase
import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.ui.layout.startup.models.AuthState
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedTextField
import com.mpieterse.stride.ui.layout.shared.components.TextFieldType
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel

@Composable
fun AuthSignInScreen(
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier,
    viewModel: AuthViewModel
) {
    val analytics = Firebase.analytics
    LaunchedEffect(Unit) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "SignIn")
        }
    }

    val formState by viewModel.signInForm.formState.collectAsState()
    val isFormValid by viewModel.signInForm.isFormValid.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    // Clear authentication error (not validation errors) when user starts typing
    LaunchedEffect(formState.identity.value, formState.password.value) {
        // Only clear auth errors, not validation errors
        val currentState = viewModel.authState.value
        if (currentState is AuthState.Error && errorMessage != null) {
            viewModel.clearError()
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
            Text(
                text = "Login",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )

            Spacer(
                Modifier.height(64.dp)
            )
            LocalOutlinedTextField(
                label = "Email Address",
                value = formState.identity.value,
                onValueChange = { value ->
                    viewModel.signInForm.onIdentityChanged(value)
                },
                modifier = Modifier.fillMaxWidth(),
                inputType = KeyboardType.Email,
                fieldType = TextFieldType.Default,
                inputAction = ImeAction.Next,
                isComponentErrored = (formState.identity.error != null),
                isComponentEnabled = !isLoading,
                textSupporting = {
                    if (formState.identity.error != null) {
                        Text(
                            text = getValidationErrorMessage(formState.identity.error!!),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            Spacer(
                Modifier.height(12.dp)
            )
            LocalOutlinedTextField(
                label = "Password",
                value = formState.password.value,
                onValueChange = { value ->
                    viewModel.signInForm.onPasswordChanged(value)
                },
                modifier = Modifier.fillMaxWidth(),
                fieldType = TextFieldType.Private,
                inputAction = ImeAction.Done,
                isComponentErrored = (formState.password.error != null),
                isComponentEnabled = !isLoading,
                textSupporting = {
                    if (formState.password.error != null) {
                        Text(
                            text = getValidationErrorMessage(formState.password.error!!),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            // Display authentication error
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )
            TextButton(
                shape = MaterialTheme.shapes.small,
                onClick = { },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 1.dp)
                    .align(Alignment.End)
            ) {
                Text(
                    text = "Forgot your password?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    color = Color(0xFF_161620),
                )
            }

            Spacer(
                Modifier.height(64.dp)
            )
            Button(
                onClick = {
                    onSignIn()
                },
                enabled = isFormValid && !isLoading,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600))
                    )
                }
            }

            Spacer(
                Modifier.height(64.dp)
            )
            IconButton(
                onClick = {
                    onGoogleSignIn()
                },
                modifier = Modifier
                    .requiredSize(56.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.xic_google
                    ),
                    modifier = Modifier.requiredSize(56.dp),
                    contentDescription = stringResource(
                        R.string.content_description_show_password
                    ),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

// Helper function to convert ValidationError to user-friendly message
private fun getValidationErrorMessage(error: ValidationError): String {
    return when (error) {
        is ValidationError.String -> when (error) {
            ValidationError.String.REQUIRE_MIN_CHARS -> "Email is too short"
            ValidationError.String.REQUIRE_MAX_CHARS -> "Email is too long"
            ValidationError.String.INVALID_EMAIL_ADDRESS -> "Please enter a valid email address"
            ValidationError.String.EXCLUDE_WHITESPACE -> "Email cannot contain spaces"
            ValidationError.String.INCLUDE_LOWERCASE -> "Password must contain lowercase letters"
            ValidationError.String.INCLUDE_UPPERCASE -> "Password must contain uppercase letters"
            ValidationError.String.INCLUDE_NUMBERS -> "Password must contain numbers"
            ValidationError.String.INCLUDE_SYMBOLS -> "Password must contain symbols"
            else -> "Invalid input"
        }
        else -> "Invalid input"
    }
}