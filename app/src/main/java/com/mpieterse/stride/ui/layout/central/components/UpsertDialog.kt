package com.mpieterse.stride.ui.layout.central.components

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mpieterse.stride.ui.layout.central.models.HabitDraft
import com.mpieterse.stride.ui.layout.shared.components.ImagePicker
import java.io.ByteArrayOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertDialog(
    title: String = "Create a Habit",
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (HabitDraft) -> Unit,
    modifier: Modifier = Modifier,
    initialData: HabitDraft? = null,
    confirmButtonLabel: String = "Add to List"
) {
    if (!isVisible) return

    val baseCategories = listOf("Health & Fitness", "Productivity", "Mindfulness", "Wellness")
    var categories by remember { mutableStateOf(baseCategories) }

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var frequencyText by remember { mutableStateOf("") }
    var frequencyError by remember { mutableStateOf<String?>(null) }
    var tagExpanded by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var imageBase64 by remember { mutableStateOf<String?>(null) }
    var imageMimeType by remember { mutableStateOf<String?>(null) }
    var imageFileName by remember { mutableStateOf<String?>(null) }

    // Reset state when dialog opens or initialData changes
    LaunchedEffect(isVisible, initialData) {
        if (isVisible) {
            categories = baseCategories
            if (initialData != null) {
                name = initialData.name
                frequencyText = initialData.frequency.toString()
                if (initialData.tag != null && initialData.tag !in baseCategories) {
                    categories = baseCategories + initialData.tag
                }
                selectedTag = initialData.tag
                imageBase64 = initialData.imageBase64
                imageMimeType = initialData.imageMimeType
                imageFileName = initialData.imageFileName
                selectedImage = initialData.imageBase64?.let { base64ToBitmap(it) }
            } else {
                name = ""
                frequencyText = ""
                selectedTag = null
                selectedImage = null
                imageBase64 = null
                imageMimeType = null
                imageFileName = null
            }
            nameError = null
            frequencyError = null
        } else {
            name = ""
            frequencyText = ""
            categories = baseCategories
            selectedTag = null
            selectedImage = null
            imageBase64 = null
            imageMimeType = null
            imageFileName = null
            nameError = null
            frequencyError = null
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Habit name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
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
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = frequencyText,
                    onValueChange = {
                        frequencyText = it.filter { ch -> ch.isDigit() }
                        if (frequencyError != null) frequencyError = null
                    },
                    label = { Text("Frequency (days/week)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = frequencyError != null,
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
                if (frequencyError != null) {
                    Text(
                        text = frequencyError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = tagExpanded,
                    onExpandedChange = { tagExpanded = it }
                ) {
                    TextField(
                        value = selectedTag ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tag") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Gray.copy(alpha = 0.7f),
                            unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = tagExpanded,
                        onDismissRequest = { tagExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedTag = category
                                    tagExpanded = false
                                }
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = "Attach an image (optional)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ImagePicker(
                        selectedImage = selectedImage,
                        onImageSelected = { bitmap ->
                            selectedImage = bitmap
                            if (bitmap != null) {
                                imageBase64 = bitmapToBase64(bitmap)
                                imageMimeType = "image/jpeg"
                                imageFileName = "habit_${UUID.randomUUID()}.jpg"
                            } else {
                                imageBase64 = null
                                imageMimeType = null
                                imageFileName = null
                            }
                        }
                    )
                    Text(
                        text = "Images are stored in BLOB storage and synced once your account reconnects.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (selectedImage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                selectedImage = null
                                imageBase64 = null
                                imageMimeType = null
                                imageFileName = null
                            }
                        ) {
                            Text("Remove image")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Please enter a name."
                        return@Button
                    }
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        nameError = "Please enter a valid habit name."
                        return@Button
                    }
                    val frequency = frequencyText.toIntOrNull()
                    if (frequency == null || frequency !in 0..7) {
                        frequencyError = "Enter a value between 0 and 7."
                        return@Button
                    }
                    onConfirm(
                        HabitDraft(
                            name = trimmedName,
                            frequency = frequency,
                            tag = selectedTag?.takeIf { it.isNotBlank() },
                            imageBase64 = imageBase64,
                            imageMimeType = imageMimeType,
                            imageFileName = imageFileName
                        )
                    )
                },
                enabled = name.trim().isNotBlank() && frequencyText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) { Text(confirmButtonLabel, color = Color.White) }
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

private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

private fun base64ToBitmap(base64: String): Bitmap? = runCatching {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()
