package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    Surface(
        color = Color(0xFF_161620),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = 40.dp,
                        topEnd = 40.dp
                    )
                )
                .padding(24.dp)
                .padding(top = 16.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight(600),
                    lineHeight = 24.sp
                )
            )
            Spacer(
                modifier = Modifier.height(32.dp)
            )


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_sync),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(16.dp)
                    )
                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )
                    Text(
                        text = "Application",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(600),
                            lineHeight = 24.sp
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                LocalOutlinedDropdown(
                    label = "Theme",
                    value = viewModel.theme,
                    onValueChange = { viewModel.updateTheme(it) },
                    items = AppAppearance.entries,
                    itemLabel = { it.name.replaceFirstChar(Char::uppercase) }
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                LocalOutlinedDropdown(
                    label = "Notifications",
                    value = viewModel.notifications,
                    onValueChange = { viewModel.updateAlerts(it) },
                    items = AlertFrequency.entries,
                    itemLabel = {
                        it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase)
                    }
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                LocalOutlinedDropdown(
                    label = "Sync Online",
                    value = viewModel.sync,
                    onValueChange = { viewModel.updateSync(it) },
                    items = SyncFrequency.entries,
                    itemLabel = {
                        it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase)
                    }
                )


                Spacer(
                    modifier = Modifier.height(56.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_external_link_alt),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(16.dp)
                    )
                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(600),
                            lineHeight = 24.sp
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                Button(
                    onClick = {
                        onEnterDebug()
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(40.dp)
                ) {
                    Text(
                        text = "Import database",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(
                                600
                            )
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                Button(
                    onClick = {},
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(40.dp)
                ) {
                    Text(
                        text = "Export database",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(
                                600
                            )
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                Button(
                    onClick = {
                        viewModel.logout()
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(40.dp)
                ) {
                    Text(
                        text = "Help & FAQ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(
                                600
                            )
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(56.dp)
                )
                Button(
                    onClick = {
                        viewModel.logout()
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(40.dp)
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(
                                600
                            )
                        )
                    )
                }
            }
        }
    }
}