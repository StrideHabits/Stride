package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(name = "HabitItem", showBackground = true)
@Composable
fun HabitChip(
    text: String = "Category"
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF_F2F2F2)
        )
    ) {
        Text(
            text = text,
            color = Color(0xFF_4B4D4F),
            modifier = Modifier
                .padding(
                    horizontal = 6.dp,
                    vertical = 2.dp
                ),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight(600),
                lineHeight = 16.sp
            )
        )
    }
}