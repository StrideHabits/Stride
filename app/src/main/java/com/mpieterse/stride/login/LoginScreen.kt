package com.mpieterse.stride.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mpieterse.stride.R

@Composable
fun LoginScreen(onContinue: () -> Unit, onRequestSignup: () -> Unit) {
    // TODO: Replace placeholder colors with design tokens once defined
    val headerHeight = 120.dp

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isFormValid = email.isNotBlank() && password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dark header matching sketch
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(Color(0xFF0F0F13))
        ) {}

        // Rounded white card body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.login_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.login_email_label)) },
                    placeholder = { Text(text = stringResource(id = R.string.login_email_placeholder)) },
                    isError = email.isNotEmpty() && !email.contains("@"),
                    colors = TextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.login_password_label)) },
                    placeholder = { Text(text = stringResource(id = R.string.login_password_placeholder)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Image(
                                painter = painterResource(id = if (passwordVisible) R.drawable.xic_uic_outline_eye_slash else R.drawable.xic_uic_outline_eye),
                                contentDescription = null
                            )
                        }
                    }
                )

                TextButton(onClick = { /* TODO(auth): trigger password reset via backend */ }, modifier = Modifier.align(Alignment.End)) {
                    Text(text = stringResource(id = R.string.login_forgot_password))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    enabled = isFormValid
                ) {
                    Text(text = stringResource(id = R.string.login_continue))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E6E6))
                    Text(text = stringResource(id = R.string.login_or), color = Color(0xFFB0B0B0))
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E6E6))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.xic_uic_outline_google),
                    contentDescription = stringResource(id = R.string.login_google_sso)
                )
                // TODO(auth): Hook Google SSO tap to Google Sign-In and JWT backend exchange

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onRequestSignup) {
                    Text(text = stringResource(id = R.string.signup_title))
                }
            }
        }
    }
}


