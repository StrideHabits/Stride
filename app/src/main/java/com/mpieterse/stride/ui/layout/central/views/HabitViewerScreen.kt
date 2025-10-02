package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.components.UpsertDialog
import com.mpieterse.stride.ui.layout.shared.components.LocalStyledActivityStatusBar
import java.time.LocalDate

@Composable
fun HabitViewerScreen(
    habitName: String = "Go to the gym",
    habitImage: android.graphics.Bitmap? = null,
    streakDays: Int = 265,
    completedDates: List<Int> = listOf(8, 18, 19, 28, 29, 30),
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Local state for habit data (functional without database)
    var currentHabitName by remember { mutableStateOf(habitName) }
    var currentHabitImage by remember { mutableStateOf(habitImage) }
    var currentStreakDays by remember { mutableStateOf(streakDays) }
    var currentCompletedDates by remember { mutableStateOf(completedDates) }
    
    // Sample habit data for editing
    val habitData = com.mpieterse.stride.ui.layout.central.components.HabitData(
        name = currentHabitName,
        frequency = 3, // days per week
        tag = "Health & Fitness",
        image = currentHabitImage
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_arrow_left),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    
                    Text(
                        text = currentHabitName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    // Empty space to balance the back button
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
            
            // Main Content
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Habit Image
                    HabitImageViewer(
                        habitImage = currentHabitImage,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Streak Banner
                    StreakBanner(
                        streakDays = currentStreakDays,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Calendar Section
                    CalendarView(
                        completedDates = currentCompletedDates,
                        modifier = Modifier.fillMaxWidth(),
                        onDateClick = { day ->
                            // Toggle completion status for the day
                            currentCompletedDates = if (day in currentCompletedDates) {
                                currentCompletedDates.filter { it != day }
                            } else {
                                currentCompletedDates + day
                            }
                        }
                    )
                }
            }
        }
        
        // Bottom Action Bar
        Surface(
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Achievement/Awards action */ }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_shield_check),
                        contentDescription = "Achievements",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Empty space for FAB
                Spacer(modifier = Modifier.width(56.dp))
            }
        }
        
        // Floating Action Button
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

    // Edit Dialog
    UpsertDialog(
        title = "Edit Habit",
        isVisible = showEditDialog,
        onDismiss = { showEditDialog = false },
        onConfirm = { updatedData ->
            // Update local state with edited data
            currentHabitName = updatedData.name
            currentHabitImage = updatedData.image
            // Keep streak and completed dates as they are (these would be calculated differently in real app)
            println("Updated habit: ${updatedData.name}")
            showEditDialog = false
        },
        initialData = habitData
    )
}

@Composable
private fun HabitImageViewer(
    habitImage: android.graphics.Bitmap?,
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Days of week header
        DaysOfWeekHeader()
        
        // Calendar grid
        CalendarGrid(
            completedDates = completedDates,
            onDateClick = onDateClick
        )
    }
}

@Composable
private fun DaysOfWeekHeader(
    modifier: Modifier = Modifier
) {
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
    // Generate calendar for current month (October 2024 based on your design)
    val currentMonth = LocalDate.of(2024, 10, 1)
    val firstDayOfMonth = currentMonth.dayOfWeek.value % 7 // Convert to 0-6 (Sunday = 0)
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // Calculate calendar grid
    val calendarDays = mutableListOf<List<Int?>>()
    
    // First week - fill empty spaces before month starts
    val firstWeek = mutableListOf<Int?>()
    repeat(firstDayOfMonth) { firstWeek.add(null) }
    for (day in 1..(7 - firstDayOfMonth)) {
        firstWeek.add(day)
    }
    calendarDays.add(firstWeek)
    
    // Remaining weeks
    var currentDay = 8 - firstDayOfMonth
    while (currentDay <= daysInMonth) {
        val week = mutableListOf<Int?>()
        repeat(7) { dayIndex ->
            if (currentDay + dayIndex <= daysInMonth) {
                week.add(currentDay + dayIndex)
            } else {
                week.add(null)
            }
        }
        calendarDays.add(week)
        currentDay += 7
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        calendarDays.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { 
                                if (day != null) {
                                    onDateClick(day)
                                }
                            },
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
