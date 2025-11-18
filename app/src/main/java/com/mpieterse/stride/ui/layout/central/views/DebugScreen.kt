package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mpieterse.stride.ui.layout.central.viewmodels.DebugViewModel
import com.mpieterse.stride.workers.PushWorker
import com.mpieterse.stride.workers.PullWorker
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    modifier: Modifier = Modifier,
    model: DebugViewModel = hiltViewModel()
) {
    val state by model.state.collectAsState()
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("Dean") }
    var email by remember { mutableStateOf("dean@example.com") }
    var pass by remember { mutableStateOf("P@ssw0rd!") }
    var habitName by remember { mutableStateOf("Read 10m") }
    var habitIdField by remember { mutableStateOf("") }
    var isoDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var filePath by remember { mutableStateOf("") }

    var habitFrequencyText by remember { mutableStateOf("1") } // parsed to Int
    var habitTag by remember { mutableStateOf("Health") }
    var habitImageUrl by remember { mutableStateOf("") }


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
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---------- AUTH ----------
                SectionCard("Auth") {
                    Text(
                        text = state.currentEmail?.let { "Signed in as $it" } ?: "Not signed in",
                        style = MaterialTheme.typography.bodyMedium
                    )
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

                // ---------- HABITS ----------
                SectionCard("Habits") {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Habit name*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = habitFrequencyText,
                            onValueChange = { new -> habitFrequencyText = new.filter { it.isDigit() }.ifEmpty { "0" } },
                            label = { Text("Frequency (days)*") },
                            supportingText = { Text("0 = flexible, 1 = daily, 3 = x3/week, 7 = weekly") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Quick presets
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Presets", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1 to "Daily", 3 to "x3/wk", 7 to "Weekly").forEach { (v, lbl) ->
                                    AssistChip(onClick = { habitFrequencyText = v.toString() }, label = { Text(lbl) })
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = habitTag,
                        onValueChange = { habitTag = it },
                        label = { Text("Tag (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = habitImageUrl,
                        onValueChange = { habitImageUrl = it },
                        label = { Text("Image URL (optional)") },
                        supportingText = { Text("Public URL; leave blank if none") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val freq = habitFrequencyText.toIntOrNull() ?: 0
                                model.createHabit(
                                    name = habitName,
                                    frequency = if (freq < 0) 0 else freq,
                                    tag = habitTag,
                                    imageUrl = habitImageUrl
                                )
                            },
                            enabled = !state.loading && habitName.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) { Text("Create") }

                        Button(
                            onClick = { model.login(email, pass, ctx) },   // was: model.login(email, pass)
                            enabled = !state.loading,
                            modifier = Modifier.weight(1f)
                        ) { Text("Login") }
                    }

                    if (state.habits.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Your Habits", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.habits) { h ->
                                ElevatedCard(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(h.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(h.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        TextButton(onClick = {
                                            habitIdField = h.id
                                            model.selectHabit(h.id, h.name)
                                        }) { Text("Use") }
                                    }
                                }
                            }
                        }
                    }
                }


                // ---------- CHECK-INS ----------
                SectionCard("Check-ins") {
                    val selected = state.selectedHabitId
                    Text(
                        text = if (selected != null) "Selected: ${state.selectedHabitName} ($selected)" else "No habit selected",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = habitIdField,
                        onValueChange = { habitIdField = it.replace("\\s".toRegex(), "") },
                        label = { Text("Habit ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(isoDate, { isoDate = it }, label = { Text("ISO Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { model.completeForDate(habitIdField.ifBlank { selected.orEmpty() }, isoDate) },
                            enabled = !state.loading,
                            modifier = Modifier.weight(1f)
                        ) { Text("Complete (custom)") }

                        Button(
                            onClick = { if (selected != null) model.quickComplete(0) },
                            enabled = !state.loading && selected != null,
                            modifier = Modifier.weight(1f)
                        ) { Text("Complete Today") }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { if (selected != null) model.quickComplete(1) },
                            enabled = !state.loading && selected != null,
                            modifier = Modifier.weight(1f)
                        ) { Text("Complete D-1") }

                        OutlinedButton(
                            onClick = { if (selected != null) model.quickComplete(2) },
                            enabled = !state.loading && selected != null,
                            modifier = Modifier.weight(1f)
                        ) { Text("Complete D-2") }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { model.listCheckIns() }, enabled = !state.loading, modifier = Modifier.weight(1f)) {
                            Text("List All (summary)")
                        }
                        OutlinedButton(
                            onClick = { /* refresh local preview for selected */ if (selected != null) { model.selectHabit(selected, state.selectedHabitName ?: "") } },
                            enabled = !state.loading && selected != null,
                            modifier = Modifier.weight(1f)
                        ) { Text("Refresh Selected Local") }
                    }

                    if (state.selectedHabitLocalDates.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Local check-ins (selected habit):", style = MaterialTheme.typography.titleSmall)
                        Card(Modifier.fillMaxWidth()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp, max = 160.dp)
                                    .padding(12.dp)
                            ) {
                                items(state.selectedHabitLocalDates) { day ->
                                    Text(day, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                                }
                            }
                        }
                    }
                }

                // ---------- SETTINGS ----------
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

                // ---------- SYNC ----------
                SectionCard("Sync") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { model.pushNow(ctx) },
                            enabled = !state.loading,
                            modifier = Modifier.weight(1f)
                        ) { Text("Push now") }

                        OutlinedButton(
                            onClick = { model.pullNow(ctx) },
                            enabled = !state.loading,
                            modifier = Modifier.weight(1f)
                        ) { Text("Pull now") }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { model.ensurePullScheduled(ctx) },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Ensure periodic pull scheduled") }
                }

                // ---------- UPLOAD ----------
                SectionCard("Upload") {
                    OutlinedTextField(filePath, { filePath = it }, label = { Text("File path") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { model.upload(filePath) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                        Text("Upload")
                    }
                }

                // ---------- LOGS ----------
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

            // Loading overlay
            AnimatedVisibility(
                visible = state.loading,
                enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
                exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
