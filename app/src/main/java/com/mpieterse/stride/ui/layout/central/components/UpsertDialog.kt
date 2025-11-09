package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun UpsertDialog(
    title: String = "Create a Habit",
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (HabitData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: HabitData? = null
) {
    if (!isVisible) return

    var name by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    // Reset state when dialog opens or initialData changes
    LaunchedEffect(isVisible, initialData?.name) {
        if (isVisible) {
            if (initialData != null) {
                // Editing existing habit - load its data
                name = initialData.name
            } else {
                // Creating new habit - start with empty name
                name = ""
            }
            errorText = null
        } else {
            // Dialog closed - reset for next time
            name = ""
            errorText = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text("Habit name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorText != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Gray.copy(alpha = 0.7f),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = Color.Gray.copy(alpha = 0.8f),
                        unfocusedLabelColor = Color.Gray.copy(alpha = 0.6f),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorText = "Please enter a name."
                        return@Button
                    }
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        errorText = "Please enter a valid habit name."
                        return@Button
                    }
                    onConfirm(HabitData(trimmedName))
                    // Do NOT auto-dismiss: mirrors Debug's "press button triggers action" feel.
                },
                enabled = name.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Create", color = Color.White) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) { Text("Cancel") }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        modifier = modifier
    )
}

data class HabitData(val name: String)
