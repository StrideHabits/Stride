package com.mpieterse.stride.ui.layout.central.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.components.UpsertDialog
import com.mpieterse.stride.ui.layout.central.components.HabitData
import com.mpieterse.stride.ui.layout.central.viewmodels.HabitViewerViewModel
import com.mpieterse.stride.utils.bitmapToBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    var initialImageBase64 by remember { mutableStateOf<String?>(null) }
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val brand = MaterialTheme.colorScheme.primary
    
    // Convert habit image to Base64 when editing dialog opens
    LaunchedEffect(showEditDialog, state.habitImage) {
        val image = state.habitImage
        initialImageBase64 = if (showEditDialog && image != null) {
            withContext(Dispatchers.Default) { 
                bitmapToBase64(image) 
            }
        } else {
            null
        }
    }
    
    // Determine MIME type from bitmap
    val initialImageMime = remember(state.habitImage) {
        state.habitImage?.let { if (it.hasAlpha()) "image/png" else "image/jpeg" }
    }

    Box(modifier = modifier.fillMaxSize().background(bg)) {
        Column(Modifier.fillMaxSize()) {
            // Top bar
            Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_arrow_left),
                            contentDescription = "Back",
                            tint = onBg
                        )
                    }
                    Text(
                        text = if (state.loading) "Loading…" else state.displayName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (state.error != null) MaterialTheme.colorScheme.error else onBg,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { vm.completeToday(habitId) },
                        enabled = !state.loading,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_check),
                            contentDescription = "Complete Today",
                            tint = brand
                        )
                    }
                }
            }

            // Main content
            Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HabitImageViewer(
                        habitImage = state.habitImage,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StreakBanner(
                        streakDays = state.streakDays,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        CalendarView(
                            completedDates = state.completedDates,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.loading,
                            onDateClick = { day ->
                                val date = LocalDate.now().withDayOfMonth(day)
                                vm.toggleCheckIn(habitId, date.toString())
                            }
                        )
                        if (state.loading) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Edit FAB
        FloatingActionButton(
            onClick = { showEditDialog = true },
            containerColor = brand,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(56.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_edit),
                contentDescription = "Edit Habit",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    UpsertDialog(
        title = "Edit Habit",
        isVisible = showEditDialog,
        onDismiss = { showEditDialog = false },
        onConfirm = { updated ->
            vm.updateLocalName(updated.name)
            vm.updateHabitDetails(updated)
            showEditDialog = false
        },
        initialData = HabitData(
            name = state.displayName,
            frequency = state.frequency,
            tag = state.tag,
            imageBase64 = initialImageBase64,
            imageMimeType = initialImageMime,
            imageFileName = null // File name can be extracted from URL if available
        ),
        isLoading = state.loading
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (habitImage != null) {
            Image(
                bitmap = habitImage.asImageBitmap(),
                contentDescription = "Habit image",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "(IMAGE)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = "$streakDays Day Streak",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarView(
    completedDates: List<Int>,
    enabled: Boolean,
    onDateClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DaysOfWeekHeader()
        CalendarGrid(completedDates = completedDates, enabled = enabled, onDateClick = onDateClick)
    }
}

@Composable
private fun DaysOfWeekHeader(modifier: Modifier = Modifier) {
    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    enabled: Boolean,
    onDateClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Monday-first offset: (value 1..7) -> 0..6 with Monday=0
    val currentMonth = remember { LocalDate.now().withDayOfMonth(1) }
    val firstDayOfMonth = remember(currentMonth) { (currentMonth.dayOfWeek.value + 6) % 7 }
    val daysInMonth = remember(currentMonth) { currentMonth.lengthOfMonth() }

    val calendarDays = remember(firstDayOfMonth, daysInMonth) {
        val rows = mutableListOf<List<Int?>>()
        val firstWeek = MutableList(firstDayOfMonth) { null as Int? }
        for (d in 1..(7 - firstDayOfMonth)) firstWeek.add(d)
        rows.add(firstWeek)
        var currentDay = 8 - firstDayOfMonth
        while (currentDay <= daysInMonth) {
            val week = MutableList(7) { idx ->
                val day = currentDay + idx
                if (day <= daysInMonth) day else null
            }
            rows.add(week)
            currentDay += 7
        }
        rows
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        calendarDays.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (day != null && enabled) Modifier.clickable { onDateClick(day) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val isDone = day in completedDates
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDone)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
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
