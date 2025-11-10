package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    
    // Refresh when screen is first created, with a small delay to allow authentication to complete
    LaunchedEffect(Unit) {
        // Small delay to allow token re-authentication to complete after unlock
        kotlinx.coroutines.delay(500)
        viewModel.refresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            color = Color(0xFF161620),
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
                        AnimatedVisibility(
                            visible = state.loading,
                            enter = fadeIn() + scaleIn(initialScale = 0.9f),
                            exit = fadeOut()
                        ) {
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
                    }
                    state.error != null -> {
                        AnimatedVisibility(
                            visible = state.error != null,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
                        ) {
                            Text(
                                text = "Error: ${state.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.habits,
                                key = { it.id },
                                contentType = { "habit" }
                            ) { row ->
                                HabitItem(
                                    cardText = row.name,
                                    chipText = row.tag,
                                    progress = row.progress,
                                    checklist = row.checklist,
                                    streaked = row.streaked,
                                    enabled = !row.pending,
                                    onClick = {
                                        if (!row.pending) onNavigateToHabitViewer(row.id)
                                    },
                                    onCheckInClick = { dayIndex ->
                                        if (!row.pending) {
                                            viewModel.checkInHabit(row.id, dayIndex)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB (stays on top) with animation
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(2f)
        ) {
            AnimatedVisibility(
                visible = !state.loading && state.error == null,
                enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + 
                        scaleIn(initialScale = 0.5f, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(200)) + 
                       scaleOut(targetScale = 0.5f, animationSpec = tween(200))
            ) {
                FloatingActionButton(
                    onClick = { showCreate = true },
                    containerColor = Color(0xFFFF9500),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Habit"
                    )
                }
            }
        }
    }

    // Dialog (root level = guaranteed visible)
    UpsertDialog(
        title = "Create a Habit",
        isVisible = showCreate,
        onDismiss = { showCreate = false },
        onConfirm = { data ->
            viewModel.createHabit(data) { ok ->
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
            Text(
                text = day,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight(600),
                    lineHeight = 16.sp
                ),
                modifier = Modifier.requiredWidth(24.dp)
            )
            if (index != days.lastIndex) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
