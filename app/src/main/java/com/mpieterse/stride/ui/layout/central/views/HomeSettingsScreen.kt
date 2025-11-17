package com.mpieterse.stride.ui.layout.central.views

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.R
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.ui.layout.central.viewmodels.HomeSettingsViewModel
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdown

@Preview(name = "Orientation H (21:9)", showBackground = true, widthDp = 1400, heightDp = 600)
@Preview(name = "Orientation V (21:9)", showBackground = true, widthDp = 600, heightDp = 1400)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeSettingsViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    onEnterDebug: () -> Unit = {}
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val culture by viewModel.locale.collectAsStateWithLifecycle()
    val notificationSettings by notificationsViewModel.state.collectAsStateWithLifecycle()
    val notificationsEnabled = notificationSettings.settings.globalNotificationsEnabled

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // Header
                Text(
                    text = stringResource(R.string.home_settings_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Application section
                SectionHeader(
                    iconRes = R.drawable.xic_uic_outline_sync,
                    title = stringResource(R.string.home_settings_application_section)
                )

                Spacer(modifier = Modifier.height(24.dp))

                LocalOutlinedDropdown(
                    label = stringResource(R.string.home_settings_theme_label),
                    value = theme,
                    onValueChange = { viewModel.updateTheme(it) },
                    items = AppAppearance.entries,
                    itemLabel = { it.name.replaceFirstChar(Char::uppercase) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Locale options
                LocalOutlinedDropdown(
                    label = stringResource(R.string.home_settings_locales_label),
                    value = culture,
                    onValueChange = {
                        viewModel.updateCulture(it)
                    },
                    items = viewModel.locales,
                    itemLabel = {
                        it
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Notifications Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_settings_notifications_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsViewModel.updateSettings(
                                notificationSettings.settings.copy(
                                    globalNotificationsEnabled = enabled
                                )
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(56.dp))

                // Options section
                SectionHeader(
                    iconRes = R.drawable.xic_uic_outline_external_link_alt,
                    title = stringResource(R.string.home_settings_options_section)
                )

                Spacer(modifier = Modifier.height(24.dp))

                val buttonColor =
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)

                Button(
                    onClick = {},
                    colors = buttonColor,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.home_settings_import_database),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(600)
                        )
                    )
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
                    Text(
                        stringResource(R.string.home_settings_export_database),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(600)
                        )
                    )
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
                    Text(
                        stringResource(R.string.home_settings_help_faq),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(600)
                        )
                    )
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
                    Text(
                        stringResource(R.string.home_settings_debug_tools),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(600)
                        )
                    )
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
                    Text(
                        stringResource(R.string.home_settings_logout),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight(600)
                        )
                    )
                }
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