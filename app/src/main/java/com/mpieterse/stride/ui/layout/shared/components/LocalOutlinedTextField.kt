package com.mpieterse.stride.ui.layout.shared.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.mpieterse.stride.R

@Composable
fun LocalOutlinedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldType: TextFieldType = TextFieldType.Default,
    inputType: KeyboardType = KeyboardType.Text,
    inputAction: ImeAction = ImeAction.Default,
    valueCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isComponentEnabled: Boolean = true,
    isComponentErrored: Boolean = false,
    useSingleLine: Boolean = true,
    useLineAmount: Int = if (useSingleLine) 1 else Int.MAX_VALUE,
    toggleIcon: @Composable (() -> Unit)? = null,
    styledIcon: @Composable (() -> Unit)? = null,
    textSupporting: @Composable (() -> Unit)? = null,
    textPlaceholder: @Composable (() -> Unit)? = null
) {
    var isValueHidden by remember { mutableStateOf(true) }

    val effectiveKeyboardType = when (fieldType) {
        TextFieldType.Private -> KeyboardType.Password
        else -> inputType
    }

    val effectiveCorrectStatus = when {
        (fieldType == TextFieldType.Private) -> false
        (inputType == KeyboardType.Password) -> false
        (inputType == KeyboardType.Email) -> false
        else -> true
    }

    // Component
    OutlinedTextField(
        label = { Text(text = label) },
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = isComponentEnabled,
        isError = isComponentErrored,
        singleLine = useSingleLine,
        maxLines = useLineAmount,
        supportingText = textSupporting,
        placeholder = textPlaceholder,
        leadingIcon = styledIcon,
        trailingIcon = toggleIcon ?: if (fieldType == TextFieldType.Private) {
            {
                PasswordToggleIcon(
                    isHidden = isValueHidden,
                    onToggled = { isValueHidden = !isValueHidden },
                    isEnabled = isComponentEnabled
                )
            }
        } else null,
        visualTransformation = when {
            ((fieldType == TextFieldType.Private) && isValueHidden) -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            capitalization = valueCapitalization,
            autoCorrectEnabled = effectiveCorrectStatus,
            keyboardType = effectiveKeyboardType,
            imeAction = inputAction
        ),
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = Color(0x10_161620),
            focusedLabelColor = Color(0xFF_161620),
            unfocusedLabelColor = Color(0xFF_161620),
            focusedBorderColor = Color(0xFF_161620),
            unfocusedBorderColor = Color(0xFF_161620),
            cursorColor = Color(0x80_161620),
            unfocusedTrailingIconColor = Color(0xFF_161620),
            focusedTrailingIconColor = Color(0xFF_161620),
            selectionColors = TextSelectionColors(
                handleColor = Color(0x80_161620),
                backgroundColor = Color(0x20_161620)
            )
        ),
    )
}


@Composable
private fun PasswordToggleIcon(
    isHidden: Boolean,
    onToggled: () -> Unit,
    isEnabled: Boolean
) {
    IconButton(
        enabled = isEnabled,
        onClick = onToggled
    ) {
        Icon(
            painter = painterResource(
                if (isHidden) R.drawable.xic_uic_outline_eye_slash
                else R.drawable.xic_uic_outline_eye
            ),
            contentDescription = stringResource(
                if (isHidden) R.string.content_description_show_password
                else R.string.content_description_hide_password
            )
        )
    }
}


enum class TextFieldType {
    Default,
    Private,
}