package com.mpieterse.stride.ui.layout.central.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.components.UpsertDialog
import com.mpieterse.stride.ui.layout.central.models.HabitDraft
import com.mpieterse.stride.ui.layout.central.viewmodels.HabitViewerViewModel
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mpieterse.stride.utils.bitmapToBase64

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

    // Use ViewModel's display name and state directly - no local state needed
    val displayName = state.displayName
    var initialImageBase64 by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(showEditDialog, state.habitImage) {
        val image = state.habitImage
        initialImageBase64 =
            if (showEditDialog && image != null) {
                withContext(Dispatchers.Default) { bitmapToBase64(image) }
            } else {
                null
            }
    }
    val initialImageMime = remember(state.habitImage) {
        // Determine MIME type based on whether bitmap has transparency
        state.habitImage?.let { if (it.hasAlpha()) "image/png" else "image/jpeg" }
    }
    val initialImageFileName: String? = remember(state.habitImageUrl) {
        state.habitImageUrl?.let { url ->
            runCatching {
                Uri.parse(url).lastPathSegment
            }.getOrNull()
        }
    }

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
                        text = if (state.loading) "Loading…" else displayName,
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
                    IconButton(
                        onClick = { vm.completeToday(habitId) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_check),
                            contentDescription = "Complete Today",
                            tint = Color(0xFF10B981)
                        )
                    }
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
                            onDateClick = { day ->
                                // Only allow clicks when not loading
                                if (!state.loading) {
                                    // Toggle check-in for this day
                                    val date = LocalDate.now().withDayOfMonth(day)
                                    vm.toggleCheckIn(habitId, date.toString())
                                }
                            }
                        )
                        
                        // Show loading overlay on calendar when creating check-ins
                        if (state.loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF9500),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }


                    AnimatedVisibility(
                        visible = state.error != null,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
                    ) {
                        if (state.error != null) {
                            Text(
                                text = state.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    
                    
                }
            }
        }

        // Edit FAB with animation
        AnimatedVisibility(
            visible = !state.loading,
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
    }

    // Simplified edit dialog — now uses only `name`
    UpsertDialog(
        title = "Edit Habit",
        isVisible = showEditDialog,
        onDismiss = { showEditDialog = false },
        onConfirm = { updated ->
            vm.updateHabit(habitId, updated)
            showEditDialog = false
        },
        initialData = HabitDraft(
            name = displayName,
            frequency = state.habitFrequency,
            tag = state.habitTag,
            imageBase64 = initialImageBase64,
            imageMimeType = initialImageMime,
            imageFileName = initialImageFileName
        ),
        confirmButtonLabel = "Save Changes"
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
    val firstDayOfMonth = (currentMonth.dayOfWeek.value % 7)
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
