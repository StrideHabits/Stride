package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
    initialData: NotificationData? = null,
    key: String? = null
) {
    if (!isVisible) return

    val context = LocalContext.current
    
    // Reset all state when dialog opens or initialData changes - use initialData?.id as key
    var habitName by remember(key, initialData?.id) { 
        mutableStateOf(
            initialData?.habitName?.takeIf { availableHabits.contains(it) }
                ?: availableHabits.firstOrNull() ?: ""
        )
    }
    
    // Initialize time from initialData or default to 09:00 (24-hour format)
    var timeHour by remember(key, initialData?.id) {
        mutableStateOf(initialData?.time?.hour?.toString()?.padStart(2, '0') ?: "09")
    }
    
    var timeMinute by remember(key, initialData?.id) {
        mutableStateOf(initialData?.time?.minute?.toString()?.padStart(2, '0') ?: "00")
    }
    
    // Initialize days from initialData or default to all days
    var selectedDays by remember(key, initialData?.id) {
        mutableStateOf(initialData?.daysOfWeek?.toSet() ?: setOf(1, 2, 3, 4, 5, 6, 7))
    }
    
    var message by remember(key, initialData?.id) {
        mutableStateOf(initialData?.message ?: "")
    }
    
    var soundEnabled by remember(key, initialData?.id) {
        mutableStateOf(initialData?.soundEnabled ?: true)
    }
    
    var vibrationEnabled by remember(key, initialData?.id) {
        mutableStateOf(initialData?.vibrationEnabled ?: true)
    }
    
    // Reset state when dialog becomes visible with new initialData
    LaunchedEffect(isVisible, initialData?.id) {
        if (isVisible && initialData != null) {
            // Reset all fields when dialog opens for editing
            habitName = initialData.habitName.takeIf { availableHabits.contains(it) }
                ?: availableHabits.firstOrNull() ?: ""
            timeHour = initialData.time.hour.toString().padStart(2, '0')
            timeMinute = initialData.time.minute.toString().padStart(2, '0')
            selectedDays = initialData.daysOfWeek.toSet()
            message = initialData.message
            soundEnabled = initialData.soundEnabled
            vibrationEnabled = initialData.vibrationEnabled
        } else if (isVisible && initialData == null) {
            // Reset to defaults when creating new notification
            habitName = availableHabits.firstOrNull() ?: ""
            timeHour = "09"
            timeMinute = "00"
            selectedDays = setOf(1, 2, 3, 4, 5, 6, 7)
            message = ""
            soundEnabled = true
            vibrationEnabled = true
        }
    }
    
    // Update habitName when availableHabits changes
    LaunchedEffect(availableHabits) {
        if (isVisible) {
            if (habitName.isNotEmpty() && !availableHabits.contains(habitName)) {
                habitName = availableHabits.firstOrNull() ?: ""
            } else if (habitName.isEmpty() && availableHabits.isNotEmpty()) {
                habitName = availableHabits.first()
            }
        }
    }
    
    // Show time picker dialog
    var showTimePicker by remember { mutableStateOf(false) }
    
    if (showTimePicker) {
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
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "$timeHour:$timeMinute",
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
                        text = "Days",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf(
                            1 to "Mon",
                            2 to "Tue",
                            3 to "Wed",
                            4 to "Thu",
                            5 to "Fri",
                            6 to "Sat",
                            7 to "Sun"
                        )
                        
                        days.forEach { (dayNumber, dayLabel) ->
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
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9500),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Black
                                )
                            )
                        }
                    }
                    
                    if (selectedDays.isEmpty()) {
                        Text(
                            text = "Please select at least one day",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Red,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp)
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
                    
                    if (habitName.isNotBlank() && availableHabits.contains(habitName) && selectedDays.isNotEmpty()) {
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

@Composable
private fun TimePickerDialog24Hour(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hour by remember { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableStateOf(initialMinute.coerceIn(0, 59)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time (24-hour)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hour (0-23)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        LocalOutlinedTextField(
                            label = "Hour",
                            value = hour.toString(),
                            onValueChange = { newValue ->
                                newValue.toIntOrNull()?.coerceIn(0, 23)?.let {
                                    hour = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            fieldType = TextFieldType.Default,
                            isComponentEnabled = true
                        )
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minute (0-59)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        LocalOutlinedTextField(
                            label = "Minute",
                            value = minute.toString(),
                            onValueChange = { newValue ->
                                newValue.toIntOrNull()?.coerceIn(0, 59)?.let {
                                    minute = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            fieldType = TextFieldType.Default,
                            isComponentEnabled = true
                        )
                    }
                }
                
                // Display current time
                Text(
                    text = "Selected: ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFF9500),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(hour, minute) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
                )
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
