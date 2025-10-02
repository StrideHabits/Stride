package com.mpieterse.stride.ui.layout.central.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import java.time.format.DateTimeFormatter

@Composable
fun NotificationItem(
    notification: NotificationData,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with habit name and toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.habitName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = if (notification.isEnabled) Color.Black else Color.Gray
                    )
                    
                    Text(
                        text = notification.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (notification.isEnabled) Color(0xFFFF9500) else Color.Gray
                    )
                }
                
                Switch(
                    checked = notification.isEnabled,
                    onCheckedChange = onToggleEnabled
                )
            }
            
            // Days of week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Days:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                notification.daysOfWeek.forEach { day ->
                    DayChip(
                        dayNumber = day,
                        isEnabled = notification.isEnabled
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
            
            // Message
            if (notification.message.isNotEmpty()) {
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (notification.isEnabled) Color.Black.copy(alpha = 0.8f) else Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
            }
            
            // Settings row (sound, vibration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_volume_up),
                        contentDescription = "Sound",
                        tint = if (notification.soundEnabled && notification.isEnabled) 
                            Color(0xFFFF9500) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sound",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (notification.isEnabled) Color.Black else Color.Gray
                    )
                }
                
                // Vibration indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xic_uic_outline_volume_down),
                        contentDescription = "Vibration",
                        tint = if (notification.vibrationEnabled && notification.isEnabled) 
                            Color(0xFFFF9500) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Vibration",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (notification.isEnabled) Color.Black else Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_edit),
                            contentDescription = "Edit",
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.xic_uic_outline_trash_alt),
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    dayNumber: Int,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = NotificationData.getShortDayName(dayNumber),
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium
        ),
        color = if (isEnabled) Color(0xFFFF9500) else Color.Gray,
        modifier = modifier
    )
}
