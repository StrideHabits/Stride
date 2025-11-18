package com.mpieterse.stride.ui.layout.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A styled ExposedDropdownMenu composable that maintains visual consistency
 * with LocalOutlinedTextField while providing dropdown functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LocalOutlinedDropdown(
    label: String,
    value: T?,
    onValueChange: (T) -> Unit,
    items: List<T>,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() },
    isComponentEnabled: Boolean = true,
    isComponentErrored: Boolean = false,
    styledIcon: @Composable (() -> Unit)? = null,
    textSupporting: @Composable (() -> Unit)? = null,
    textPlaceholder: @Composable (() -> Unit)? = null,
    itemContent: @Composable ((T) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = value?.let { itemLabel(it) } ?: ""

    val colorScheme = MaterialTheme.colorScheme
    val placeholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val selectionColors = TextSelectionColors(
        handleColor = colorScheme.primary,
        backgroundColor = colorScheme.primary.copy(alpha = 0.35f)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (isComponentEnabled) {
                expanded = it
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            label = { Text(text = label) },
            value = displayText,
            onValueChange = { },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            enabled = isComponentEnabled,
            isError = isComponentErrored,
            readOnly = true,
            singleLine = true,
            supportingText = textSupporting,
            placeholder = textPlaceholder,
            leadingIcon = styledIcon,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                disabledTextColor = colorScheme.onSurface.copy(alpha = 0.4f),
                cursorColor = colorScheme.primary,
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                disabledBorderColor = colorScheme.outline.copy(alpha = 0.4f),
                focusedLabelColor = colorScheme.primary,
                unfocusedLabelColor = colorScheme.onSurfaceVariant,
                disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                focusedPlaceholderColor = placeholderColor,
                unfocusedPlaceholderColor = placeholderColor,
                disabledPlaceholderColor = placeholderColor.copy(alpha = 0.5f),
                focusedLeadingIconColor = colorScheme.primary,
                unfocusedLeadingIconColor = colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                focusedTrailingIconColor = colorScheme.primary,
                unfocusedTrailingIconColor = colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                selectionColors = selectionColors
            ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            shape = MaterialTheme.shapes.large
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        if (itemContent != null) {
                            itemContent(item)
                        } else {
                            Text(
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/**
 * Simplified version for String lists
 */
@Composable
fun LocalOutlinedDropdownStringOnly(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    items: List<String>,
    modifier: Modifier = Modifier,
    isComponentEnabled: Boolean = true,
    isComponentErrored: Boolean = false,
    styledIcon: @Composable (() -> Unit)? = null,
    textSupporting: @Composable (() -> Unit)? = null,
    textPlaceholder: @Composable (() -> Unit)? = null
) {
    LocalOutlinedDropdown(
        label = label,
        value = value.ifEmpty { null },
        onValueChange = onValueChange,
        items = items,
        modifier = modifier,
        itemLabel = { it },
        isComponentEnabled = isComponentEnabled,
        isComponentErrored = isComponentErrored,
        styledIcon = styledIcon,
        textSupporting = textSupporting,
        textPlaceholder = textPlaceholder
    )
}