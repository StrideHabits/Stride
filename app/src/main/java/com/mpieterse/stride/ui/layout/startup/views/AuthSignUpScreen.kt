package com.mpieterse.stride.ui.layout.startup.views

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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.mpieterse.stride.core.validation.ValidationError
import com.mpieterse.stride.ui.layout.startup.models.AuthState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val isFormValid by viewModel.signUpForm.isFormValid.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message
    
    // Check password match
    val passwordsMatch = formState.passwordDefault.value == formState.passwordConfirm.value
    val showPasswordMatchError = formState.passwordConfirm.value.isNotEmpty() && !passwordsMatch

    // Clear authentication error only in direct response to user input (handled in onValueChange)

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
                text = "Sign Up",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )

            Spacer(
                Modifier.height(64.dp)
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        tint = Color(0xFF_161620),
                        painter = painterResource(R.drawable.xic_uic_outline_user_plus),
                        contentDescription = "",
                        modifier = Modifier.requiredSize(16.dp)
                    )
                    Spacer(
                        Modifier.width(12.dp)
                    )
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    )
                }
                Spacer(
                    Modifier.height(12.dp)
                )
                LocalOutlinedTextField(
                    label = "Email address",
                    value = formState.identity.value,
                    onValueChange = { value ->
                        viewModel.signUpForm.onIdentityChanged(value)
                        if (errorMessage != null) viewModel.clearError()
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
                                text = getEmailValidationErrorMessage(formState.identity.error!!),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
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
                        tint = Color(0xFF_161620),
                        painter = painterResource(R.drawable.xic_uic_outline_shield),
                        contentDescription = "",
                        modifier = Modifier.requiredSize(16.dp)
                    )
                    Spacer(
                        Modifier.width(12.dp)
                    )
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    )
                }

                Spacer(
                    Modifier.height(12.dp)
                )
                LocalOutlinedTextField(
                    label = "Create a strong password",
                    value = formState.passwordDefault.value,
                    onValueChange = { value ->
                        viewModel.signUpForm.onPasswordDefaultChanged(value)
                        if (errorMessage != null) viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Private,
                    inputAction = ImeAction.Next,
                    isComponentErrored = (formState.passwordDefault.error != null),
                    isComponentEnabled = !isLoading,
                    textSupporting = {
                        if (formState.passwordDefault.error != null) {
                            Text(
                                text = getPasswordValidationErrorMessage(formState.passwordDefault.error!!),
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
                    label = "Confirm your password",
                    value = formState.passwordConfirm.value,
                    onValueChange = { value ->
                        viewModel.signUpForm.onPasswordConfirmChanged(value)
                        if (errorMessage != null) viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Private,
                    inputAction = ImeAction.Done,
                    isComponentErrored = (formState.passwordConfirm.error != null || showPasswordMatchError),
                    isComponentEnabled = !isLoading,
                    textSupporting = {
                        if (formState.passwordConfirm.error != null) {
                            Text(
                                text = getPasswordValidationErrorMessage(formState.passwordConfirm.error!!),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (showPasswordMatchError) {
                            Text(
                                text = "Passwords do not match",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }

            Spacer(
                Modifier.height(64.dp)
            )
            
            // Display authentication error
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            
            Column {
                Button(
                    onClick = {
                        onSignUp()
                    },
                    enabled = isFormValid && passwordsMatch && !isLoading,
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
                            text = "Sign Up",
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
                    text = "Have an account? Login",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    color = Color(0xFF_161620),
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
                    text = "Help + FAQ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    color = Color(0xFF_161620),
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

// Helper functions to convert ValidationError to user-friendly messages
private fun getEmailValidationErrorMessage(error: ValidationError): String {
    return when (error) {
        is ValidationError.String -> when (error) {
            ValidationError.String.INVALID_EMAIL_ADDRESS -> "Please enter a valid email address"
            ValidationError.String.EXCLUDE_WHITESPACE -> "Email cannot contain spaces"
            ValidationError.String.REQUIRE_MIN_CHARS -> "Email is too short"
            ValidationError.String.REQUIRE_MAX_CHARS -> "Email is too long"
            else -> "Invalid email"
        }
        else -> "Invalid email"
    }
}

private fun getPasswordValidationErrorMessage(error: ValidationError): String {
    return when (error) {
        is ValidationError.String -> when (error) {
            ValidationError.String.REQUIRE_MIN_CHARS -> "Password must be at least 6 characters"
            ValidationError.String.REQUIRE_MAX_CHARS -> "Password is too long"
            ValidationError.String.EXCLUDE_WHITESPACE -> "Password cannot contain spaces"
            ValidationError.String.INCLUDE_LOWERCASE -> "Password must contain lowercase letters"
            ValidationError.String.INCLUDE_UPPERCASE -> "Password must contain uppercase letters"
            ValidationError.String.INCLUDE_NUMBERS -> "Password must contain numbers"
            ValidationError.String.INCLUDE_SYMBOLS -> "Password must contain symbols"
            else -> "Invalid password"
        }
        else -> "Invalid password"
    }
}