package com.mpieterse.stride.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpieterse.stride.R

data class HabitUi(
    val id: String,
    val title: String,
    val tag: String?,
    val recentFourDays: List<Boolean>, // order: oldest..newest
    val score: Int = 0,
    val hasStreak: Boolean = false
)

@Composable
fun HomeScreen(onCreateHabit: () -> Unit, onOpenSettings: () -> Unit, onOpenHabit: (String) -> Unit) {
    // TODO(data): Replace with ViewModel + repository
    val sample = remember {
        listOf(
            HabitUi("1", "Hit the gym", "Health", listOf(true, false, true, true), score = 72, hasStreak = true),
            HabitUi("2", "Drink 150ml water", null, listOf(false, false, false, false), score = 10),
            HabitUi("3", "Read 2 pages", null, listOf(true, true, false, true), score = 55)
        )
    }

    var showUpsert by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showUpsert = true; onCreateHabit() }, containerColor = Color(0xFFFF9800)) {
                Icon(painter = painterResource(id = R.drawable.ic_add_fab), contentDescription = stringResource(R.string.home_fab_desc))
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header row: weekdays mini-grid (placeholder circles)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        WeekHeader()
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(sample) { habit -> HabitRow(habit, onOpenHabit) }
                    }
                }
            }
        }
    }

    if (showUpsert) {
        HabitUpsertDialog(onDismiss = { showUpsert = false }, onConfirm = { name, freq, tag, image ->
            // TODO(data): save habit to local DB and queue sync
            showUpsert = false
        })
    }
}

@Composable
private fun WeekHeader() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) {
            Box(modifier = Modifier.height(12.dp).background(Color.Transparent))
            Text(text = "◯", color = Color.Gray)
        }
    }
}

@Composable
private fun HabitRow(habit: HabitUi, onOpenHabit: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenHabit(habit.id) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: score ring placeholder
        Text(text = "⟳", color = Color(0xFFFF9800), modifier = Modifier.padding(end = 12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = habit.title, style = MaterialTheme.typography.bodyLarge)
            habit.tag?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            habit.recentFourDays.forEach { done ->
                Text(text = if (done) "●" else "○", color = if (done) Color(0xFFFF9800) else Color.Gray)
            }
        }
    }
}


