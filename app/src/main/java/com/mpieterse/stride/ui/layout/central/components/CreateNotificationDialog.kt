package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.AnimatedVisibility
import com.mpieterse.stride.ui.animations.fadeInTransition
import com.mpieterse.stride.ui.animations.fadeOutTransition
import com.mpieterse.stride.ui.animations.expandVerticallyTransition
import com.mpieterse.stride.ui.animations.shrinkVerticallyTransition
import kotlinx.coroutines.launch
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdownStringOnly
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedTextField
import com.mpieterse.stride.ui.layout.shared.components.TextFieldType
import java.time.LocalTime
import java.util.UUID

@Composable
fun CreateNotificationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (NotificationData) -> Unit,
    availableHabits: List<String> = listOf("Go to the gym", "Read for 30 minutes", "Meditation", "Drink water", "Exercise"),
    initialData: NotificationData? = null
) {
    val context = LocalContext.current
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeInTransition(duration = 200),
        exit = fadeOutTransition(duration = 150)
    ) {
    
    // Initialize state - will be reset when dialog opens
    var habitName by remember { mutableStateOf("") }
    var timeHour by remember { mutableStateOf("09") }
    var timeMinute by remember { mutableStateOf("00") }
    var selectedDays by remember { mutableStateOf<Set<Int>>(setOf()) }
    var message by remember { mutableStateOf("") }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    
    // Load/reset state when dialog opens or data changes
    LaunchedEffect(isVisible, initialData?.id) {
        if (isVisible) {
            if (initialData != null) {
                // Load existing notification data
                habitName = initialData.habitName.takeIf { availableHabits.contains(it) }
                    ?: availableHabits.firstOrNull() ?: ""
                timeHour = initialData.time.hour.toString().padStart(2, '0')
                timeMinute = initialData.time.minute.toString().padStart(2, '0')
                selectedDays = initialData.daysOfWeek.toSet()
                message = initialData.message
                soundEnabled = initialData.soundEnabled
                vibrationEnabled = initialData.vibrationEnabled
            } else {
                // Reset to defaults for new notification
                habitName = "" // Start empty, user must select a habit
                timeHour = "09"
                timeMinute = "00"
                selectedDays = setOf() // Start with no days selected - user must choose
                message = ""
                soundEnabled = true
                vibrationEnabled = true
            }
        }
    }
    
    // Cleanup when dialog leaves composition (on dismiss)
    // Note: onDispose runs when this effect leaves composition, which happens when isVisible goes from true -> false.
    // Therefore, reset state unconditionally inside onDispose.
    DisposableEffect(isVisible) {
        onDispose {
            habitName = ""
            timeHour = "09"
            timeMinute = "00"
            selectedDays = setOf()
            message = ""
            soundEnabled = true
            vibrationEnabled = true
        }
    }
    
    // Note: We don't auto-select a habit when creating new notifications
    // User must explicitly select a habit from the dropdown
    
    // Show time picker dialog
    var showTimePicker by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = showTimePicker,
        enter = fadeInTransition(duration = 200),
        exit = fadeOutTransition(duration = 150)
    ) {
        TimePickerDialog24Hour(
            initialHour = timeHour.toIntOrNull() ?: 9,
            initialMinute = timeMinute.toIntOrNull() ?: 0,
            onTimeSelected = { hour, minute ->
                timeHour = hour.toString().padStart(2, '0')
                timeMinute = minute.toString().padStart(2, '0')
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (initialData != null) "Edit Habit Reminder" else "Add Habit Reminder",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Habit Selection
                if (availableHabits.isEmpty()) {
                    Text(
                        text = "No habits available. Please create a habit first.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column {
                        LocalOutlinedDropdownStringOnly(
                            label = "Habit",
                            value = habitName,
                            onValueChange = { habitName = it },
                            items = availableHabits,
                            modifier = Modifier.fillMaxWidth(),
                            isComponentEnabled = availableHabits.isNotEmpty(),
                            textPlaceholder = {
                                Text(
                                    text = if (availableHabits.isEmpty()) "No habits available" else "Select a habit",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                        
                        // Show error if habit is not selected when creating new notification
                        AnimatedVisibility(
                            visible = initialData == null && habitName.isBlank() && availableHabits.isNotEmpty(),
                            enter = expandVerticallyTransition(),
                            exit = shrinkVerticallyTransition()
                        ) {
                            Text(
                                text = "Please select a habit",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Red,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Time Selection (24-hour format)
                Column {
                    Text(
                        text = "Time (24-hour format)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time display button that opens picker
                        // Show placeholder when creating new notification with default time
                        val timeDisplayText = if (initialData == null && timeHour == "09" && timeMinute == "00") {
                            "Select time"
                        } else {
                            "${timeHour.padStart(2, '0')}:${timeMinute.padStart(2, '0')}"
                        }
                        
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (initialData == null && timeHour == "09" && timeMinute == "00") {
                                    Color.Gray
                                } else {
                                    Color.Black
                                }
                            )
                        ) {
                            Text(
                                text = timeDisplayText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Days Selection
                Column {
                    Text(
                        text = "Days of Week",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Use a more spacious layout with two rows
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Better layout: 7 days in a flexible grid with proper spacing
                        // First row: Mon, Tue, Wed, Thu (4 items)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val firstRow = listOf(
                                1 to "Mon",
                                2 to "Tue",
                                3 to "Wed",
                                4 to "Thu"
                            )
                            
                            firstRow.forEach { (dayNumber, dayLabel) ->
                                val isSelected = selectedDays.contains(dayNumber)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected) {
                                            selectedDays - dayNumber
                                        } else {
                                            selectedDays + dayNumber
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = dayLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            ),
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF9500),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF5F5F5),
                                        labelColor = if (isSelected) Color.White else Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                        
                        // Second row: Fri, Sat, Sun (3 items with spacer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val secondRow = listOf(
                                5 to "Fri",
                                6 to "Sat",
                                7 to "Sun"
                            )
                            
                            secondRow.forEach { (dayNumber, dayLabel) ->
                                val isSelected = selectedDays.contains(dayNumber)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected) {
                                            selectedDays - dayNumber
                                        } else {
                                            selectedDays + dayNumber
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = dayLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            ),
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF9500),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF5F5F5),
                                        labelColor = if (isSelected) Color.White else Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            // Add spacer to balance the layout
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = selectedDays.isEmpty(),
                        enter = expandVerticallyTransition(),
                        exit = shrinkVerticallyTransition()
                    ) {
                        Text(
                            text = "Please select at least one day",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Red,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Message
                LocalOutlinedTextField(
                    label = "Message (Optional)",
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    fieldType = TextFieldType.Default,
                    isComponentEnabled = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hour = timeHour.toIntOrNull()?.coerceIn(0, 23) ?: 9
                    val minute = timeMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    
                    // Validation is already handled by button enabled state,
                    // but double-check here for safety
                    if (habitName.isNotBlank() && 
                        availableHabits.contains(habitName) && 
                        selectedDays.isNotEmpty()) {
                        val notification = NotificationData(
                            id = initialData?.id ?: UUID.randomUUID().toString(),
                            habitName = habitName,
                            time = LocalTime.of(hour, minute),
                            daysOfWeek = selectedDays.sorted(),
                            isEnabled = initialData?.isEnabled ?: true,
                            message = message,
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled
                        )
                        
                        onConfirm(notification)
                        onDismiss()
                    }
                },
                enabled = habitName.isNotBlank() && 
                         availableHabits.isNotEmpty() && 
                         availableHabits.contains(habitName) &&
                         selectedDays.isNotEmpty(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
                ),
                modifier = Modifier.width(130.dp)
            ) {
                Text(
                    text = if (initialData != null) "Update" else "Add",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                modifier = Modifier.width(90.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        modifier = Modifier.fillMaxWidth(0.95f)
    )
    }
}

@Composable
private fun TimePickerDialog24Hour(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableStateOf(initialMinute.coerceIn(0, 59)) }
    val coroutineScope = rememberCoroutineScope()
    
    // Create lists for hours (0-23) and minutes (0-59)
    val hours = (0..23).toList()
    val minutes = (0..59).toList()
    
    // Lazy list states for scrolling
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hour.coerceIn(0, 23))
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minute.coerceIn(0, 59))
    
    // Sync scroll position with selected values when they change externally
    LaunchedEffect(initialHour) {
        if (initialHour != hour) {
            hourListState.animateScrollToItem(initialHour.coerceIn(0, 23))
        }
    }
    
    LaunchedEffect(initialMinute) {
        if (initialMinute != minute) {
            minuteListState.animateScrollToItem(initialMinute.coerceIn(0, 59))
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display selected time prominently
                Text(
                    text = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9500)
                    )
                )
                
                // Scrollable hour and minute pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hour",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ScrollableNumberPicker(
                            items = hours,
                            selectedValue = hour,
                            onValueSelected = { hour = it },
                            listState = hourListState,
                            modifier = Modifier.height(200.dp)
                        )
                    }
                    
                    // Colon separator
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minute picker
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Minute",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ScrollableNumberPicker(
                            items = minutes,
                            selectedValue = minute,
                            onValueSelected = { minute = it },
                            listState = minuteListState,
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(hour, minute) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ScrollableNumberPicker(
    items: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Track if we're programmatically scrolling to prevent infinite loops
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    var lastFirstVisibleItem by remember { mutableStateOf(-1) }
    var lastScrollTime by remember { mutableStateOf(0L) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Initialize the lastFirstVisibleItem on first composition
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Wait for layout
        lastFirstVisibleItem = listState.firstVisibleItemIndex
        isInitialized = true
    }
    
    // Detect scroll changes and snap to center when scrolling stops (only for user-initiated scrolls)
    LaunchedEffect(listState.firstVisibleItemIndex) {
        // Don't process until initialized
        if (!isInitialized) return@LaunchedEffect
        
        val currentFirstVisible = listState.firstVisibleItemIndex
        val currentTime = System.currentTimeMillis()
        
        // Only process if:
        // 1. The first visible item changed
        // 2. We're not in the middle of a programmatic scroll
        // 3. Enough time has passed since last scroll (debounce)
        if (currentFirstVisible != lastFirstVisibleItem && 
            !isProgrammaticScroll && 
            (currentTime - lastScrollTime) > 300) {
            lastFirstVisibleItem = currentFirstVisible
            lastScrollTime = currentTime
            
            // Wait for scroll to settle, then snap to center
            kotlinx.coroutines.delay(300)
            
            // Check if still not programmatically scrolling (user might have scrolled again)
            if (!isProgrammaticScroll) {
                val layoutInfo = listState.layoutInfo
                if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    val center = layoutInfo.viewportSize.height / 2
                    val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                        val itemCenter = item.offset + item.size / 2
                        kotlin.math.abs(itemCenter - center)
                    }
                    
                    centerItem?.let { item ->
                        val index = item.index
                        if (index in items.indices && items[index] != selectedValue) {
                            isProgrammaticScroll = true
                            coroutineScope.launch {
                                // Snap to the center item
                                listState.animateScrollToItem(index)
                                // Update selected value
                                onValueSelected(items[index])
                                // Reset flag after animation completes
                                kotlinx.coroutines.delay(500)
                                isProgrammaticScroll = false
                                lastFirstVisibleItem = index
                            }
                        } else {
                            // Item is already selected, just update lastFirstVisibleItem
                            lastFirstVisibleItem = index
                        }
                    }
                }
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        // Selection indicator (highlighted area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Color(0xFFFF9500).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 76.dp) // Padding to center items
        ) {
            items(items.size) { index ->
                val value = items[index]
                val isSelected = value == selectedValue
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            isProgrammaticScroll = true
                            onValueSelected(value)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                kotlinx.coroutines.delay(500)
                                isProgrammaticScroll = false
                                lastFirstVisibleItem = index
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = if (isSelected) 24.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFFF9500) else Color.Gray
                        )
                    )
                }
            }
        }
    }
}
