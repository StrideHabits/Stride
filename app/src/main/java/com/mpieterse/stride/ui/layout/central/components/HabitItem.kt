package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpieterse.stride.R


// --- Composers


@Preview(name = "HabitItem", showBackground = true, widthDp = 400)
@Composable
fun HabitItem(
    cardText: String = "",
    chipText: String = "",
    progress: Float = 0F,
    streaked: Boolean = false,
    checklist: List<Boolean> = emptyList(),
    onClick: () -> Unit = {},
    onCheckInClick: (Int) -> Unit = {}
) {
    Column {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                ProgressIcon(
                    progress = progress,
                    streaked = streaked
                )

                HabitText(
                    cardText = cardText,
                    chipText = chipText,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick() }
                )

                if (checklist.isNotEmpty()) {
                    HabitTicks(
                        states = checklist,
                        onClick = onCheckInClick
                    )
                }
            }
        }
    }
}


// --- Internals


@Composable
private fun ProgressIcon(
    progress: Float,
    streaked: Boolean
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.requiredSize(36.dp),
            trackColor = Color(0xFF_FFE8C8),
            strokeCap = StrokeCap.Round,
        )

        if (streaked) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_fire),
                contentDescription = null,
                modifier = Modifier.requiredSize(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
private fun HabitText(
    cardText: String,
    chipText: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = cardText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp
            )
        )

        if (chipText.isNotEmpty()) {
            HabitChip(text = chipText)
        }
    }
}