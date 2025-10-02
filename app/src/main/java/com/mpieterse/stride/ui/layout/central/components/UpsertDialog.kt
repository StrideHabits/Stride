package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mpieterse.stride.ui.layout.shared.components.ImagePicker
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdownStringOnly

@Composable
fun UpsertDialog(
    title: String = "Create a Habit",
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (HabitData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: HabitData? = null
) {
    if (isVisible) {
        var name by remember { mutableStateOf(initialData?.name ?: "") }
        var frequency by remember { mutableStateOf(initialData?.frequency?.toString() ?: "") }
        var selectedTag by remember { mutableStateOf(initialData?.tag ?: "") }
        var selectedImage by remember { mutableStateOf<android.graphics.Bitmap?>(initialData?.image) }

        val availableTags = listOf(
            "Health & Fitness",
            "Productivity",
            "Learning",
            "Mindfulness",
            "Social",
            "Career",
            "Finance",
            "Hobbies",
            "Home & Family",
            "Other"
        )

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
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        placeholder = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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

                    // Frequency Field
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frequency (days/week)") },
                        placeholder = { Text("Frequency (days/week)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                    // Tag Dropdown
                    Column {
                        Text(
                            text = "Tag",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LocalOutlinedDropdownStringOnly(
                            label = "",
                            value = selectedTag,
                            onValueChange = { selectedTag = it },
                            items = availableTags,
                            modifier = Modifier.fillMaxWidth(),
                            isComponentEnabled = true
                        )
                    }

                    // Image Attachment Area
                    ImagePicker(
                        selectedImage = selectedImage,
                        onImageSelected = { selectedImage = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (validateInputs(name, frequency, selectedTag)) {
                            val habitData = HabitData(
                                name = name,
                                frequency = frequency.toIntOrNull() ?: 1,
                                tag = selectedTag,
                                image = selectedImage
                            )
                            onConfirm(habitData)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9500) // Orange color from theme
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(130.dp)
                        .height(40.dp)
                ) {
                    Text(
                        text = "Add to List",
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(90.dp)
                        .height(40.dp)
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
                dismissOnClickOutside = false
            ),
            modifier = modifier
        )
    }
}

private fun validateInputs(name: String, frequency: String, tag: String): Boolean {
    return name.isNotBlank() && 
           frequency.isNotBlank() && 
           frequency.toIntOrNull() != null && 
           tag.isNotBlank()
}

data class HabitData(
    val name: String,
    val frequency: Int, // days per week
    val tag: String,
    val image: android.graphics.Bitmap? = null
)
