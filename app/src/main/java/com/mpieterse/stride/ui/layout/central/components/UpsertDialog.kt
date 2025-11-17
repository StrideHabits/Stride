package com.mpieterse.stride.ui.layout.central.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.shared.components.ImagePicker
import com.mpieterse.stride.ui.layout.shared.components.LocalOutlinedDropdownStringOnly
import com.mpieterse.stride.utils.bitmapToBase64
import com.mpieterse.stride.utils.base64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun UpsertDialog(
    title: String = stringResource(R.string.upsert_dialog_create_title),
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (HabitData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: HabitData? = null,
    isLoading: Boolean = false
) {
    if (!isVisible) return

    // Common tag options - include current tag if it exists and isn't in the list
    val tagHealthFitness = stringResource(R.string.tag_health_fitness)
    val tagProductivity = stringResource(R.string.tag_productivity)
    val tagLearning = stringResource(R.string.tag_learning)
    val tagMindfulness = stringResource(R.string.tag_mindfulness)
    val tagSocial = stringResource(R.string.tag_social)
    val tagFinance = stringResource(R.string.tag_finance)
    val tagCreative = stringResource(R.string.tag_creative)
    val tagPersonalCare = stringResource(R.string.tag_personal_care)
    val tagRelationships = stringResource(R.string.tag_relationships)
    val tagHobbies = stringResource(R.string.tag_hobbies)
    
    val baseTagOptions = listOf(
        tagHealthFitness,
        tagProductivity,
        tagLearning,
        tagMindfulness,
        tagSocial,
        tagFinance,
        tagCreative,
        tagPersonalCare,
        tagRelationships,
        tagHobbies
    )
    val currentTag = initialData?.tag
    val tagOptions = if (currentTag != null && currentTag !in baseTagOptions) {
        baseTagOptions + currentTag
    } else {
        baseTagOptions
    }
    
    var name by remember { mutableStateOf(initialData?.name ?: "") }
    var frequency by remember { mutableStateOf(initialData?.frequency?.takeIf { it > 0 }?.toString() ?: "1") }
    var selectedTag by remember { mutableStateOf(initialData?.tag ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val nameErrorText = stringResource(R.string.upsert_dialog_name_error)
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var imageBase64 by remember { mutableStateOf<String?>(initialData?.imageBase64) }
    var imageMimeType by remember { mutableStateOf<String?>(initialData?.imageMimeType) }
    var imageFileName by remember { mutableStateOf<String?>(initialData?.imageFileName) }
    
    // Load initial image from Base64 if provided
    LaunchedEffect(initialData?.imageBase64) {
        if (initialData?.imageBase64 != null && selectedImage == null) {
            val decoded = withContext(Dispatchers.Default) {
                base64ToBitmap(initialData.imageBase64)
            }
            decoded?.let { selectedImage = it }
        }
    }

    // Convert selected image to Base64 when it changes
    LaunchedEffect(selectedImage) {
        val bitmap = selectedImage
        if (bitmap != null) {
            val (base64, mime) = withContext(Dispatchers.Default) {
                val encoded = bitmapToBase64(bitmap)
                val mimeType = if (bitmap.hasAlpha()) "image/png" else "image/jpeg"
                encoded to mimeType
            }
            if (base64 != null) {
                imageBase64 = base64
                imageMimeType = mime
            } else {
                imageBase64 = null
                imageMimeType = null
            }
        } else {
            imageBase64 = null
            imageMimeType = null
            imageFileName = null
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
        exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
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
                    label = { Text(stringResource(R.string.upsert_dialog_name_label)) },
                    placeholder = { Text(stringResource(R.string.upsert_dialog_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorText != null && name.isBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                    label = { Text(stringResource(R.string.upsert_dialog_frequency_label)) },
                    placeholder = { Text(stringResource(R.string.upsert_dialog_frequency_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Tag Dropdown
                LocalOutlinedDropdownStringOnly(
                    label = stringResource(R.string.upsert_dialog_tag_label),
                    value = selectedTag,
                    onValueChange = { selectedTag = it },
                    items = tagOptions,
                    modifier = Modifier.fillMaxWidth(),
                    isComponentEnabled = true,
                    textPlaceholder = {
                        Text(
                            text = stringResource(R.string.upsert_dialog_select_tag),
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
                errorText?.let { error ->
                    Text(
                        text = error,
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
                        errorText = nameErrorText
                        return@Button
                    }
                    val frequencyValue = frequency.toIntOrNull()?.coerceIn(1, 7) ?: 1
                    onConfirm(
                        HabitData(
                            name = name.trim(),
                            frequency = frequencyValue,
                            tag = selectedTag.takeUnless { it.isBlank() },
                            imageBase64 = imageBase64,
                            imageMimeType = imageMimeType,
                            imageFileName = imageFileName
                        )
                    )
                    // Do NOT auto-dismiss: mirrors Debug's "press button triggers action" feel.
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (initialData != null) stringResource(R.string.upsert_dialog_update_button) else stringResource(R.string.upsert_dialog_add_button), 
                            color = MaterialTheme.colorScheme.onPrimary
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
                    )
                ) { Text(stringResource(R.string.upsert_dialog_cancel_button)) }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            ),
            modifier = modifier
        )
    }
}

data class HabitData(
    val name: String,
    val frequency: Int = 1,
    val tag: String? = null,
    val imageBase64: String? = null,
    val imageMimeType: String? = null,
    val imageFileName: String? = null
)
