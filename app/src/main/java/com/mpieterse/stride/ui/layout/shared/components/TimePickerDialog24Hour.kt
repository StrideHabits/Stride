package com.mpieterse.stride.ui.layout.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import java.util.Locale
import java.time.LocalTime

/**
 * 24-hour format time picker dialog with scrollable number pickers.
 * 
 * Features:
 * - Scrollable hour picker (0-23)
 * - Scrollable minute picker (0-59)
 * - Large time display (48sp, bold, orange)
 * - Click-to-select functionality
 */
@Composable
fun TimePickerDialog24Hour(
    initialTime: LocalTime? = null,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHour = initialTime?.hour ?: 9
    val initialMinute = initialTime?.minute ?: 0
    
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    val hours = (0..23).toList()
    val minutes = (0..59).toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Large time display
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9500)
                    ),
                    textAlign = TextAlign.Center
                )
                
                // Time pickers row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    ScrollableNumberPicker(
                        items = hours,
                        selectedValue = selectedHour,
                        onValueSelected = { selectedHour = it },
                        initialIndex = initialHour,
                        label = "Hour",
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                    
                    // Minute picker
                    ScrollableNumberPicker(
                        items = minutes,
                        selectedValue = selectedMinute,
                        onValueSelected = { selectedMinute = it },
                        initialIndex = initialMinute,
                        label = "Min",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(LocalTime.of(selectedHour, selectedMinute))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("OK", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

@Composable
private fun ScrollableNumberPicker(
    items: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    initialIndex: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex.coerceIn(0, items.size - 1))
    val itemHeight = 48.dp
    
    // Update selected value when scroll position changes (center item follows scroll)
    LaunchedEffect(listState.firstVisibleItemIndex, listState.isScrollInProgress) {
        // Wait for scrolling to stop before updating
        if (!listState.isScrollInProgress) {
            delay(100) // Small delay to ensure scroll has settled
            // Calculate center item: firstVisibleItemIndex + 1 (middle of 3 visible items)
            val centerIndex = (listState.firstVisibleItemIndex + 1).coerceIn(0, items.size - 1)
            val centerItem = items[centerIndex]
            if (centerItem != selectedValue) {
                onValueSelected(centerItem)
            }
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = Color.Gray
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .height(144.dp) // 3 items visible
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 48.dp) // Center padding
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = item == selectedValue
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .clickable {
                                onValueSelected(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", item),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (isSelected) 20.sp else 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFFF9500) else Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Selection overlay (visual highlight for center item)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center)
                    .background(Color(0xFFFF9500).copy(alpha = 0.1f))
            )
        }
    }
}
