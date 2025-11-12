package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.ui.layout.central.components.HabitItem
import com.mpieterse.stride.ui.layout.central.components.UpsertDialog
import com.mpieterse.stride.ui.layout.central.viewmodels.HomeDatabaseViewModel

@Composable
fun HomeDatabaseScreen( //This composable displays the main habit tracking screen using Jetpack Compose Material Design (Google Inc., 2024).
    modifier: Modifier = Modifier,
    onNavigateToHabitViewer: (String) -> Unit = {},
    viewModel: HomeDatabaseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCreate by rememberSaveable { mutableStateOf(false) }
    
    // Refresh when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

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
                    .fillMaxSize()
            ) {
                // Status bar + refresh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(state.status) },
                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }
                    )
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text("Refresh")
                    }
                }

                DateHeader(
                    modifier = Modifier.align(Alignment.End),
                    days = state.daysHeader
                )

                when {
                    state.loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    state.error != null -> {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.habits, key = { it.id }) { row ->
                                HabitItem(
                                    cardText = row.name,
                                    chipText = row.tag,
                                    progress = row.progress,
                                    checklist = row.checklist,
                                    streaked = row.streaked,
                                    onClick = { onNavigateToHabitViewer(row.id) },
                                    onCheckInClick = { dayIndex ->
                                        viewModel.checkInHabit(row.id, dayIndex)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB (stays on top)
        FloatingActionButton(
            onClick = { showCreate = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(2f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Habit"
            )
        }
    }

    // Dialog (root level = guaranteed visible)
    UpsertDialog(
        title = "Create a Habit",
        isVisible = showCreate,
        onDismiss = { showCreate = false },
        onConfirm = { data ->
            viewModel.createHabit(
                name = data.name,
                frequency = data.frequency,
                tag = data.tag,
                imageBase64 = data.imageBase64,
                imageMimeType = data.imageMimeType,
                imageUrl = null
            ) { ok ->
                if (ok) showCreate = false
            }
        }
    )
}

@Composable
private fun DateHeader(
    days: List<String>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(horizontal = 12.dp)) {
        days.forEachIndexed { index, day ->
            val parts = day.split("\n")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.requiredWidth(32.dp)
            ) {
                Text(
                    text = parts.getOrNull(0) ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = parts.getOrNull(1) ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
            if (index != days.lastIndex) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
