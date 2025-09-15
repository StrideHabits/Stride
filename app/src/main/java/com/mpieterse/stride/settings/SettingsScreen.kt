package com.mpieterse.stride.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpieterse.stride.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onImport: () -> Unit, onExport: () -> Unit, onHelp: () -> Unit, onLogout: () -> Unit) {
    var themeExpanded by remember { mutableStateOf(false) }
    var theme by remember { mutableStateOf("System default") }
    var notificationsExpanded by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf("Send everything") }
    var syncExpanded by remember { mutableStateOf(false) }
    var sync by remember { mutableStateOf("Always") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth().background(Color.White, shape = MaterialTheme.shapes.medium).padding(12.dp)) {
            Text(text = stringResource(R.string.settings_section_application), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Theme
            LabeledDropdown(label = stringResource(R.string.settings_theme), selected = theme, expanded = themeExpanded,
                onExpandedChange = { themeExpanded = it }, options = listOf("System default", "Light", "Dark")) { theme = it }

            Spacer(modifier = Modifier.height(8.dp))

            // Notifications
            LabeledDropdown(label = stringResource(R.string.settings_notifications), selected = notifications, expanded = notificationsExpanded,
                onExpandedChange = { notificationsExpanded = it }, options = listOf("Send everything", "Only reminders", "Silent")) { notifications = it }

            Spacer(modifier = Modifier.height(8.dp))

            // Sync
            LabeledDropdown(label = stringResource(R.string.settings_sync), selected = sync, expanded = syncExpanded,
                onExpandedChange = { syncExpanded = it }, options = listOf("Always", "Wi‑Fi only", "Manual")) { sync = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth().background(Color.White, shape = MaterialTheme.shapes.medium).padding(12.dp)) {
            Text(text = stringResource(R.string.settings_section_options), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.settings_import))
                TextButton(onClick = { onImport(); /* TODO(data): import database */ }) { Text(text = stringResource(R.string.settings_action_import)) }
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.settings_export))
                TextButton(onClick = { onExport(); /* TODO(data): export database */ }) { Text(text = stringResource(R.string.settings_action_export)) }
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.settings_help))
                TextButton(onClick = { onHelp(); /* TODO(ui): open help */ }) { Text(text = stringResource(R.string.settings_action_open)) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = { onLogout(); /* TODO(auth): logout and clear session */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                Text(text = stringResource(R.string.settings_logout))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledDropdown(label: String, selected: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, options: List<String>, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                options.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); onExpandedChange(false) })
                }
            }
        }
    }
}


