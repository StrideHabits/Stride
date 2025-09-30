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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedTextField
import com.mpieterse.stride.ui.layout.shared.components.TextFieldType
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel

@Composable
fun AuthSignUpScreen(
    onAuthenticated: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    model: AuthViewModel,
    modifier: Modifier
) {
    var identityField by remember { mutableStateOf("") }
    var passwordDefaultField by remember { mutableStateOf("") }
    var passwordConfirmField by remember { mutableStateOf("") }

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
                    value = identityField,
                    onValueChange = { value ->
                        identityField = value
                    },
                    modifier = Modifier.fillMaxWidth(),
                    inputType = KeyboardType.Email,
                    fieldType = TextFieldType.Default,
                    inputAction = ImeAction.Next,
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
                    value = passwordDefaultField,
                    onValueChange = { value ->
                        passwordDefaultField = value
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Private,
                    inputAction = ImeAction.Next,
                )
                
                Spacer(
                    Modifier.height(12.dp)
                )
                
                LocalOutlinedTextField(
                    label = "Confirm your password",
                    value = passwordConfirmField,
                    onValueChange = { value ->
                        passwordConfirmField = value
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Private,
                    inputAction = ImeAction.Done,
                )
            }

            Spacer(
                Modifier.height(64.dp)
            )
            Column {
                Button(
                    onClick = { /* ... */ },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(52.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600))
                    )
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
                        .clickable { /* ... */ }
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