package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HabitTicks(
    states: List<Boolean>,
    onClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        states.forEachIndexed { index, selected ->
            RadioButton(
                selected = selected,
                onClick = { onClick(index) },
                modifier = Modifier.requiredSize(24.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )

            if (index != states.lastIndex) Spacer(
                modifier = Modifier.width(8.dp)
            )
        }
    }
}