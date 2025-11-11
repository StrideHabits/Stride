package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdown
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
    availableHabits: List<HabitDto> = emptyList(),
    initialData: NotificationData? = null
) {
    if (!isVisible) return

    var selectedHabit by remember { 
        mutableStateOf<HabitDto?>(
            initialData?.habitId?.let { id -> 
                availableHabits.firstOrNull { it.id == id }
            }
        )
    }
    var habitName by remember { mutableStateOf(initialData?.habitName ?: selectedHabit?.name ?: "") }
    var timeHour by remember { mutableStateOf(initialData?.time?.hour?.let { 
        if (it == 0) "12" else if (it > 12) (it - 12).toString() else it.toString() 
    } ?: "9") }
    var timeMinute by remember { mutableStateOf(initialData?.time?.minute?.toString()?.padStart(2, '0') ?: "00") }
    var timeAmPm by remember { mutableStateOf(initialData?.time?.hour?.let { if (it >= 12) "PM" else "AM" } ?: "AM") }
    var selectedDays by remember { mutableStateOf<Set<Int>>(initialData?.daysOfWeek?.toSet() ?: setOf()) }
    var message by remember { mutableStateOf(initialData?.message ?: "") }
    var soundEnabled by remember { mutableStateOf(initialData?.soundEnabled ?: true) }
    var vibrationEnabled by remember { mutableStateOf(initialData?.vibrationEnabled ?: true) }
    
    // Update selected days when initial data changes
    androidx.compose.runtime.LaunchedEffect(initialData?.daysOfWeek) {
        if (initialData != null) {
            selectedDays = initialData.daysOfWeek.toSet()
        }
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
                LocalOutlinedDropdown(
                    label = "Habit",
                    value = selectedHabit,
                    onValueChange = { habit ->
                        selectedHabit = habit
                        habitName = habit.name
                    },
                    items = availableHabits,
                    itemLabel = { it.name },
                    modifier = Modifier.fillMaxWidth(),
                    isComponentEnabled = availableHabits.isNotEmpty(),
                    textPlaceholder = {
                        Text(
                            text = if (availableHabits.isEmpty()) "No habits available" else "Select a habit",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )

                // Time Selection
                Column {
                    Text(
                        text = "Time",
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
                        LocalOutlinedTextField(
                            label = "Hour",
                            value = timeHour,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.toIntOrNull() in 1..12) {
                                    timeHour = newValue
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            fieldType = TextFieldType.Default,
                            isComponentEnabled = true
                        )
                        
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        LocalOutlinedTextField(
                            label = "Min",
                            value = timeMinute,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 2 && newValue.toIntOrNull() in 0..59) {
                                    timeMinute = newValue.padStart(2, '0')
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            fieldType = TextFieldType.Default,
                            isComponentEnabled = true
                        )
                        
                        LocalOutlinedDropdownStringOnly(
                            label = "",
                            value = timeAmPm,
                            onValueChange = { timeAmPm = it },
                            items = listOf("AM", "PM"),
                            modifier = Modifier.weight(1f),
                            isComponentEnabled = true
                        )
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
                    
                    // Two-row layout for days
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // First row: Mon, Tue, Wed, Thu
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
                        
                        // Second row: Fri, Sat, Sun
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
                            // Add spacer to balance layout
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    // Show error if no days selected
                    AnimatedVisibility(
                        visible = selectedDays.isEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
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
                enabled = selectedHabit != null && habitName.isNotBlank() && selectedDays.isNotEmpty(),
                onClick = {
                    if (selectedHabit != null && habitName.isNotBlank() && selectedDays.isNotEmpty()) {
                        val hour = timeHour.toIntOrNull() ?: 9
                        val minute = timeMinute.toIntOrNull() ?: 0
                        val isPM = timeAmPm == "PM"
                        val adjustedHour = if (isPM && hour != 12) hour + 12 else if (!isPM && hour == 12) 0 else hour
                        
                        val notification = NotificationData(
                            id = initialData?.id ?: UUID.randomUUID().toString(),
                            habitId = selectedHabit?.id,
                            habitName = habitName,
                            time = LocalTime.of(adjustedHour, minute),
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
