package com.mpieterse.stride.ui.layout.startup.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.mpieterse.stride.R

@Composable
fun AuthLaunchScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier,
) {
    val showContent by remember { mutableStateOf(true) }
    val analytics = Firebase.analytics
    LaunchedEffect(Unit) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Onboarding")
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = showContent, animationSpec = tween(durationMillis = TransitionConfig.SLOW_DURATION)) { visible ->
                if (visible) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Stride Logo",
                    modifier = Modifier.size(120.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Welcome Message
                Text(
                    text = "Welcome to Stride",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Build better habits, one stride at a time",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(64.dp))
                
                // Sign In Button
                Button(
                    onClick = onNavigateToSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sign Up Button
                OutlinedButton(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    }
                }
            }
            }
        }
    }
}