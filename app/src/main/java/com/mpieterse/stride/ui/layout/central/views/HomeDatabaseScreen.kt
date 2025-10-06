package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.ui.layout.central.components.HabitItem
import com.mpieterse.stride.ui.layout.central.viewmodels.HomeDatabaseViewModel

@Preview(name = "Orientation H (21:9)", showBackground = true, widthDp = 1400, heightDp = 600)
@Preview(name = "Orientation V (21:9)", showBackground = true, widthDp = 600, heightDp = 1400)
@Composable
fun HomeDatabaseScreen(
    modifier: Modifier = Modifier,
    onNavigateToHabitViewer: (String) -> Unit = {},
    viewModel: HomeDatabaseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // ensures first load when coming back to the screen
        viewModel.refresh()
    }

    Surface(
        color = Color(0xFF_161620),
        modifier = modifier
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                DateHeader(
                    modifier = Modifier.align(Alignment.End),
                    days = state.daysHeader
                )

                when {
                    state.loading -> {
                        Text(
                            text = "Loading…",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
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
                                    onClick = { onNavigateToHabitViewer(row.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Internals (unchanged) ---
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
