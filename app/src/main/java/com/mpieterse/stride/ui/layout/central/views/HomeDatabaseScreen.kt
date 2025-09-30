package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpieterse.stride.ui.layout.central.components.HabitItem

@Preview(name = "Orientation H (21:9)", showBackground = true, widthDp = 1400, heightDp = 600)
@Preview(name = "Orientation V (21:9)", showBackground = true, widthDp = 600, heightDp = 1400)
@Composable
fun HomeDatabaseScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF_161620),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = 40.dp,
                        topEnd = 40.dp
                    )
                )
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                DateHeader(
                    modifier = Modifier.align(Alignment.End),
                    days = listOf(
                        "SUN\n19",
                        "SAT\n18",
                        "FRI\n17"
                    )
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = modifier
                        .fillMaxSize()
                ) {
                    items(3) {
                        HabitItem(
                            cardText = "Attend one daily gym session",
                            chipText = "Health",
                            progress = 0.25F,
                            checklist = listOf(true, false, false),
                            streaked = true
                        )
                    }
                }
            }
        }
    }
}


// --- Internals


@Composable
private fun DateHeader(
    days: List<String>,
    modifier: Modifier = Modifier 
) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
    ) {
        days.forEachIndexed { index, day ->
            Text(
                text = day,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight(600),
                    lineHeight = 16.sp
                ),
                modifier = Modifier
                    .requiredWidth(24.dp)
            )

            if (index != days.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
