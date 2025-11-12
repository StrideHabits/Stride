package com.mpieterse.stride.ui.layout.central.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mpieterse.stride.ui.layout.shared.components.ImagePicker
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdownStringOnly
import com.mpieterse.stride.utils.bitmapToBase64
import com.mpieterse.stride.utils.base64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // Common tag options - include current tag if it exists and isn't in the list
    val baseTagOptions = listOf(
        "Health & Fitness",
        "Productivity",
        "Learning",
        "Mindfulness",
        "Social",
        "Finance",
        "Creative",
        "Personal Care",
        "Relationships",
        "Hobbies"
    )
    val currentTag = initialData?.tag
    val tagOptions = remember(currentTag) {
        if (currentTag != null && currentTag !in baseTagOptions) {
            baseTagOptions + currentTag
        } else {
            baseTagOptions
        }
    }
    
    var name by remember { mutableStateOf(initialData?.name ?: "") }
    var frequency by remember { mutableStateOf(initialData?.frequency?.toString() ?: "0") }
    var selectedTag by remember { mutableStateOf(initialData?.tag ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var imageBase64 by remember { mutableStateOf<String?>(initialData?.imageBase64) }
    var imageMimeType by remember { mutableStateOf<String?>(initialData?.imageMimeType) }
    var imageFileName by remember { mutableStateOf<String?>(initialData?.imageFileName) }
    
    // Load initial image from Base64 if provided
    LaunchedEffect(initialData?.imageBase64) {
        if (initialData?.imageBase64 != null && selectedImage == null) {
            withContext(Dispatchers.Default) {
                base64ToBitmap(initialData.imageBase64)?.let {
                    selectedImage = it
                }
            }
        }
    }
    
    // Convert selected image to Base64 when it changes
    LaunchedEffect(selectedImage) {
        if (selectedImage != null) {
            withContext(Dispatchers.Default) {
                bitmapToBase64(selectedImage!!)?.let { base64 ->
                    imageBase64 = base64
                    // Determine MIME type based on bitmap format
                    imageMimeType = if (selectedImage!!.hasAlpha()) {
                        "image/png"
                    } else {
                        "image/jpeg"
                    }
                }
            }
        } else {
            imageBase64 = null
            imageMimeType = null
            imageFileName = null
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
                modifier = Modifier
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text("Name") },
                    placeholder = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorText != null && name.isBlank(),
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
                    onValueChange = { newValue ->
                        // Only allow digits
                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                            frequency = newValue
                            if (errorText != null) errorText = null
                        }
                    },
                    label = { Text("Frequency (days/week)") },
                    placeholder = { Text("Frequency (days/week)") },
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
                
                // Tag Dropdown
                LocalOutlinedDropdownStringOnly(
                    label = "Tag",
                    value = selectedTag,
                    onValueChange = { selectedTag = it },
                    items = tagOptions,
                    modifier = Modifier.fillMaxWidth(),
                    isComponentEnabled = true,
                    textPlaceholder = {
                        Text(
                            text = "Select a tag",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
                
                // Image Picker
                ImagePicker(
                    selectedImage = selectedImage,
                    onImageSelected = { bitmap ->
                        selectedImage = bitmap
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Error message
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
                    val frequencyValue = frequency.toIntOrNull() ?: 0
                    onConfirm(
                        HabitData(
                            name = name.trim(),
                            frequency = frequencyValue.coerceAtLeast(0),
                            tag = selectedTag.takeUnless { it.isBlank() },
                            imageBase64 = imageBase64,
                            imageMimeType = imageMimeType,
                            imageFileName = imageFileName
                        )
                    )
                    // Do NOT auto-dismiss: mirrors Debug's "press button triggers action" feel.
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500)),
                shape = RoundedCornerShape(8.dp)
            ) { 
                Text(
                    text = if (initialData != null) "Update" else "Add to List", 
                    color = Color.White
                ) 
            }
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

data class HabitData(
    val name: String,
    val frequency: Int = 0,
    val tag: String? = null,
    val imageBase64: String? = null,
    val imageMimeType: String? = null,
    val imageFileName: String? = null
)
