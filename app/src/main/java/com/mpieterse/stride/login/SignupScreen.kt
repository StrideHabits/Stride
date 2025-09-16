package com.mpieterse.stride.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
fun SignupScreen(onCreateAccount: () -> Unit, onLoginLink: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && surname.isNotBlank() &&
            email.contains("@") && password.length >= 8 && password == confirmPassword

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
                .background(Color(0xFF0F0F13))
        ) {}

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.signup_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Account section
                Text(text = stringResource(R.string.signup_section_account), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.signup_name_placeholder)) },
                        colors = TextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.signup_surname_placeholder)) },
                        colors = TextFieldDefaults.colors()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.signup_email_placeholder)) },
                    isError = email.isNotEmpty() && !email.contains("@"),
                    colors = TextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Security section
                Text(text = stringResource(R.string.signup_section_security), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.signup_password_placeholder)) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Image(
                                painter = painterResource(id = if (showPassword) R.drawable.xic_uic_outline_eye_slash else R.drawable.xic_uic_outline_eye),
                                contentDescription = null
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.signup_confirm_placeholder)) },
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Image(
                                painter = painterResource(id = if (showConfirm) R.drawable.xic_uic_outline_eye_slash else R.drawable.xic_uic_outline_eye),
                                contentDescription = null
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCreateAccount,
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(text = stringResource(R.string.signup_cta))
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onLoginLink) {
                    Text(text = stringResource(R.string.signup_login_link))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Google icon-only button (per sketch)
                Image(painter = painterResource(id = R.drawable.xic_uic_outline_google), contentDescription = stringResource(R.string.signup_google_sso))

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { /* TODO(ui): open help */ }) {
                    Text(text = stringResource(id = R.string.settings_help))
                }

                // TODO(auth): Register via credentials with backend and Google SSO
            }
        }
    }
}


