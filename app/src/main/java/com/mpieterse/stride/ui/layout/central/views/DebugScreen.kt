package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mpieterse.stride.ui.layout.central.viewmodels.DebugViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    modifier: Modifier = Modifier,
    model: DebugViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("Dean") }
    var email by remember { mutableStateOf("dean@example.com") }
    var pass by remember { mutableStateOf("P@ssw0rd!") }
    var habitName by remember { mutableStateOf("Read 10m") }
    var habitId by remember { mutableStateOf("") }
    var isoDate by remember { mutableStateOf("2025-10-03") }
    var filePath by remember { mutableStateOf("") }

    val state by model.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SummitAPI — Debug") },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = { Text(state.status) },
                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }

                    )
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AUTH
            SectionCard("Auth") {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    pass, { pass = it }, label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { model.register(name, email, pass) }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("Register")
                    }
                    Button(onClick = { model.login(email, pass) }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("Login")
                    }
                }
            }

            // HABITS
            SectionCard("Habits") {
                OutlinedTextField(habitName, { habitName = it }, label = { Text("Habit name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { model.createHabit(habitName) }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("Create")
                    }
                    Button(onClick = { model.listHabits() }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("List")
                    }
                }
            }

            // CHECK-INS
            SectionCard("Check-ins") {
                OutlinedTextField(
                    value = habitId,
                    onValueChange = { habitId = it.replace("\\s".toRegex(), "") },
                    label = { Text("Habit ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(isoDate, { isoDate = it }, label = { Text("ISO Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { model.completeToday(habitId, isoDate) }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("Complete Today")
                    }
                    Button(onClick = { model.listCheckIns() }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("List Check-ins")
                    }
                }
            }

            // SETTINGS
            SectionCard("Settings") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { model.getSettings() }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("Get")
                    }
                    Button(onClick = { model.updateSettings(9, "system") }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                        Text("Set 9 / system")
                    }
                }
            }

            // UPLOAD
            SectionCard("Upload") {
                OutlinedTextField(filePath, { filePath = it }, label = { Text("File path") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { model.upload(filePath) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                    Text("Upload")
                }
            }

            // OUTPUT / LOGS (high-contrast, scrollable)
            Text("Output", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 280.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    items(state.logs) { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
