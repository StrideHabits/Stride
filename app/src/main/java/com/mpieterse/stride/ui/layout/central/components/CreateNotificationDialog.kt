package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.mpieterse.stride.ui.layout.shared.components.TimePickerDialog24Hour
import java.time.LocalTime
import java.util.UUID

@Composable
fun CreateNotificationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (NotificationData) -> Unit,
    availableHabits: List<HabitDto> = emptyList(),
    initialData: NotificationData? = null,
    isLoading: Boolean = false
) {
    var selectedHabit by remember { 
        mutableStateOf<HabitDto?>(
            initialData?.habitId?.let { id -> 
                availableHabits.firstOrNull { it.id == id }
            }
        )
    }
    var habitName by remember { mutableStateOf(initialData?.habitName ?: selectedHabit?.name ?: "") }
    var selectedTime by remember { 
        mutableStateOf<LocalTime?>(initialData?.time ?: LocalTime.of(9, 0))
    }
    var showTimePicker by remember { mutableStateOf(false) }
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
    
    // Reset time picker state when dialog closes
    androidx.compose.runtime.LaunchedEffect(isVisible) {
        if (!isVisible) {
            showTimePicker = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
        exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (initialData != null) "Edit Habit Reminder" else "Add Habit Reminder",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
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

                // Time Selection (24-hour format)
                Column {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Time display button that opens time picker
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = selectedTime?.let { 
                                String.format("%02d:%02d", it.hour, it.minute)
                            } ?: "Select time",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (selectedTime != null) 18.sp else 14.sp,
                                fontWeight = if (selectedTime != null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTime != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Days Selection
                Column {
                    Text(
                        text = "Days of Week",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)) + expandVertically(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
                        exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION)) + shrinkVertically(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
                    ) {
                        Text(
                            text = "Please select at least one day",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error,
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
                enabled = !isLoading && selectedHabit != null && habitName.isNotBlank() && selectedDays.isNotEmpty() && selectedTime != null,
                onClick = {
                    if (selectedHabit != null && habitName.isNotBlank() && selectedDays.isNotEmpty() && selectedTime != null) {
                        val notification = NotificationData(
                            id = initialData?.id ?: UUID.randomUUID().toString(),
                            habitId = selectedHabit?.id,
                            habitName = habitName,
                            time = selectedTime!!,
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
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.width(130.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (initialData != null) "Update" else "Add",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
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
    
    // 24-hour time picker dialog
    if (showTimePicker) {
        TimePickerDialog24Hour(
            initialTime = selectedTime,
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
