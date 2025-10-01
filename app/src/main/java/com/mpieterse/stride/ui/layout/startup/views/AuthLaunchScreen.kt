package com.mpieterse.stride.ui.layout.startup.views

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel

@Composable
fun AuthLaunchScreen(
    model: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier,
) {

// --- UI

    Surface(
        color = Color(0xFF_161620),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
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
            Button(
                onClick = {
                    onNavigateToSignIn()
                },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.screen_auth_launch_navigate_to_sign_in),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600))
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )
            Button(
                onClick = {
                    onNavigateToSignUp()
                },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.screen_auth_launch_navigate_to_sign_up),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600))
                )
            }
        }
    }
}