package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.R
import com.mpieterse.stride.core.models.configuration.options.AlertFrequency
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.core.models.configuration.options.SyncFrequency
import com.mpieterse.stride.ui.layout.central.viewmodels.HomeSettingsViewModel
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdown

@Preview(name = "Orientation H (21:9)", showBackground = true, widthDp = 1400, heightDp = 600)
@Preview(name = "Orientation V (21:9)", showBackground = true, widthDp = 600, heightDp = 1400)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeSettingsViewModel = hiltViewModel(),
    onEnterDebug: () -> Unit = {}
) {
    // ✅ Collect state flows as Compose states
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val sync by viewModel.sync.collectAsStateWithLifecycle()

    Surface(
        color = Color(0xFF161620),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                )
                .padding(24.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            // Header
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight(600),
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Application section
            SectionHeader(
                iconRes = R.drawable.xic_uic_outline_sync,
                title = "Application"
            )

            Spacer(modifier = Modifier.height(24.dp))

            LocalOutlinedDropdown(
                label = "Theme",
                value = theme,
                onValueChange = { viewModel.updateTheme(it) },
                items = AppAppearance.entries,
                itemLabel = { it.name.replaceFirstChar(Char::uppercase) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LocalOutlinedDropdown(
                label = "Notifications",
                value = notifications,
                onValueChange = { viewModel.updateAlerts(it) },
                items = AlertFrequency.entries,
                itemLabel = { it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LocalOutlinedDropdown(
                label = "Sync Online",
                value = sync,
                onValueChange = { viewModel.updateSync(it) },
                items = SyncFrequency.entries,
                itemLabel = { it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase) }
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Options section
            SectionHeader(
                iconRes = R.drawable.xic_uic_outline_external_link_alt,
                title = "Options"
            )

            Spacer(modifier = Modifier.height(24.dp))

            val buttonColor = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500))

            Button(
                onClick = {},
                colors = buttonColor,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            ) {
                Text("Import database", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600)))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                colors = buttonColor,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            ) {
                Text("Export database", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600)))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                colors = buttonColor,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            ) {
                Text("Help & FAQ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600)))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEnterDebug() },
                colors = buttonColor,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            ) {
                Text("Debug Tools", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600)))
            }

            Spacer(modifier = Modifier.height(56.dp))

            Button(
                onClick = {
                    viewModel.logout()
                },
                colors = buttonColor,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            ) {
                Text("Logout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(600)))
            }
        }
    }
}

@Composable
private fun SectionHeader(iconRes: Int, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.requiredSize(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight(600),
                lineHeight = 24.sp
            )
        )
    }
}