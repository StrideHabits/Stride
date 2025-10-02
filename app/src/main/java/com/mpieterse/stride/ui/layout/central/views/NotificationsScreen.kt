package com.mpieterse.stride.ui.layout.central.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.components.CreateNotificationDialog
import com.mpieterse.stride.ui.layout.central.components.NotificationItem
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationFrequency
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import java.time.LocalTime

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier
) {
    // Local state for notifications (no database)
    var notifications by remember { 
        mutableStateOf(
            listOf(
                NotificationData(
                    id = "1",
                    habitName = "Go to the gym",
                    time = LocalTime.of(7, 0),
                    daysOfWeek = listOf(1, 3, 5), // Mon, Wed, Fri
                    isEnabled = true,
                    message = "Time for your workout! 💪",
                    soundEnabled = true,
                    vibrationEnabled = true
                ),
                NotificationData(
                    id = "2",
                    habitName = "Read for 30 minutes",
                    time = LocalTime.of(21, 0),
                    daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7), // Daily
                    isEnabled = true,
                    message = "Reading time! 📚",
                    soundEnabled = true,
                    vibrationEnabled = false
                ),
                NotificationData(
                    id = "3",
                    habitName = "Meditation",
                    time = LocalTime.of(8, 30),
                    daysOfWeek = listOf(2, 4, 6), // Tue, Thu, Sat
                    isEnabled = false,
                    message = "Start your day with mindfulness 🧘",
                    soundEnabled = false,
                    vibrationEnabled = true
                )
            )
        )
    }
    
    var showCreateNotificationDialog by remember { mutableStateOf(false) }
    var showEditNotificationDialog by remember { mutableStateOf(false) }
    var notificationToEdit by remember { mutableStateOf<NotificationData?>(null) }
    var notificationSettings by remember { 
        mutableStateOf(
            NotificationSettings(
                globalNotificationsEnabled = true,
                defaultSoundEnabled = true,
                defaultVibrationEnabled = true,
                quietHoursStart = LocalTime.of(22, 0),
                quietHoursEnd = LocalTime.of(7, 0),
                quietHoursEnabled = true
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxSize()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Color.Black
            )
            
            // Global Settings Card
            NotificationSettingsCard(
                settings = notificationSettings,
                onSettingsChange = { notificationSettings = it }
            )
            
            // Notifications List
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Habit Reminders",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                if (notifications.isEmpty()) {
                    // Empty state
                    EmptyNotificationsState(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onToggleEnabled = { enabled ->
                                    notifications = notifications.map { 
                                        if (it.id == notification.id) {
                                            it.copy(isEnabled = enabled)
                                        } else it
                                    }
                                },
                                onEdit = {
                                    notificationToEdit = notification
                                    showEditNotificationDialog = true
                                },
                                onDelete = {
                                    notifications = notifications.filter { it.id != notification.id }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateNotificationDialog = true },
            containerColor = Color(0xFFFF9500),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_plus),
                contentDescription = "Add Notification",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
    
    // Create Notification Dialog
    CreateNotificationDialog(
        isVisible = showCreateNotificationDialog,
        onDismiss = { showCreateNotificationDialog = false },
        onConfirm = { newNotification ->
            notifications = notifications + newNotification
            showCreateNotificationDialog = false
        }
    )
    
    // Edit Notification Dialog
    CreateNotificationDialog(
        isVisible = showEditNotificationDialog,
        onDismiss = { 
            showEditNotificationDialog = false
            notificationToEdit = null
        },
        onConfirm = { updatedNotification ->
            notifications = notifications.map { 
                if (it.id == updatedNotification.id) updatedNotification else it 
            }
            showEditNotificationDialog = false
            notificationToEdit = null
        },
        initialData = notificationToEdit
    )
}

@Composable
private fun NotificationSettingsCard(
    settings: NotificationSettings,
    onSettingsChange: (NotificationSettings) -> Unit,
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
            Text(
                text = "Global Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Black
            )
            
            // Global notifications toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Switch(
                    checked = settings.globalNotificationsEnabled,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(globalNotificationsEnabled = it))
                    }
                )
            }
            
            // Default sound toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Default Sound",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Switch(
                    checked = settings.defaultSoundEnabled,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(defaultSoundEnabled = it))
                    }
                )
            }
            
            // Default vibration toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Default Vibration",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Switch(
                    checked = settings.defaultVibrationEnabled,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(defaultVibrationEnabled = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsState(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_bell),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
            
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black
            )
            
            Text(
                text = "Add notifications to get reminded about your habits",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
