package com.mpieterse.stride.ui.layout.central.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.components.UpsertDialog
import com.mpieterse.stride.ui.layout.central.components.HabitData
import com.mpieterse.stride.ui.layout.central.viewmodels.HabitViewerViewModel
import java.time.LocalDate

@Composable
fun HabitViewerScreen(
    habitId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val vm: HabitViewerViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(habitId) { vm.load(habitId) }

    var showEditDialog by remember { mutableStateOf(false) }

    // mirror VM state locally for edit preview (no API write yet)
    var currentHabitName by remember(state.habitName) { mutableStateOf(state.habitName) }
    var currentHabitImage by remember(state.habitImage) { mutableStateOf<Bitmap?>(state.habitImage) }
    var currentStreakDays by remember(state.streakDays) { mutableStateOf(state.streakDays) }
    var currentCompletedDates by remember(state.completedDates) { mutableStateOf(state.completedDates) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            // Top bar
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_arrow_left),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = if (state.loading) "Loading…" else currentHabitName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (state.error != null) MaterialTheme.colorScheme.error else Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(48.dp))
                }
            }

            // Main content
            Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HabitImageViewer(
                        habitImage = currentHabitImage,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StreakBanner(
                        streakDays = currentStreakDays,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CalendarView(
                        completedDates = currentCompletedDates,
                        modifier = Modifier.fillMaxWidth(),
                        onDateClick = { day ->
                            currentCompletedDates =
                                if (day in currentCompletedDates) currentCompletedDates - day
                                else currentCompletedDates + day
                        }
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Edit FAB
        FloatingActionButton(
            onClick = { showEditDialog = true },
            containerColor = Color(0xFFFF9500),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_edit),
                contentDescription = "Edit Habit",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Simplified edit dialog — now uses only `name`
    UpsertDialog(
        title = "Edit Habit",
        isVisible = showEditDialog,
        onDismiss = { showEditDialog = false },
        onConfirm = { updated ->
            currentHabitName = updated.name
            showEditDialog = false
        },
        initialData = HabitData(name = currentHabitName)
    )
}

/* ---------- Helpers ---------- */

@Composable
private fun HabitImageViewer(
    habitImage: Bitmap?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.1f))
    ) {
        if (habitImage != null) {
            Image(
                bitmap = habitImage.asImageBitmap(),
                contentDescription = "Habit image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "(IMAGE)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StreakBanner(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFF9500).copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = "$streakDays Day Streak!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9500),
                fontSize = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarView(
    completedDates: List<Int>,
    onDateClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DaysOfWeekHeader()
        CalendarGrid(completedDates = completedDates, onDateClick = onDateClick)
    }
}

@Composable
private fun DaysOfWeekHeader(modifier: Modifier = Modifier) {
    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    fontSize = 12.sp
                ),
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    completedDates: List<Int>,
    onDateClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentMonth = LocalDate.now().withDayOfMonth(1)
    val firstDayOfMonth = currentMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    val calendarDays = mutableListOf<List<Int?>>()
    val firstWeek = MutableList(firstDayOfMonth) { null as Int? }
    for (d in 1..(7 - firstDayOfMonth)) firstWeek.add(d)
    calendarDays.add(firstWeek)

    var currentDay = 8 - firstDayOfMonth
    while (currentDay <= daysInMonth) {
        val week = MutableList(7) { idx ->
            val day = currentDay + idx
            if (day <= daysInMonth) day else null
        }
        calendarDays.add(week)
        currentDay += 7
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        calendarDays.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { if (day != null) onDateClick(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (day in completedDates) FontWeight.Bold else FontWeight.Normal,
                                    color = if (day in completedDates) Color(0xFFFF9500) else Color.Gray,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
