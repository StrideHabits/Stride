package com.mpieterse.stride.ui.layout.central.views

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.size
import com.mpieterse.stride.R
import com.mpieterse.stride.core.permissions.NotificationPermissionHelper
import com.mpieterse.stride.ui.layout.central.components.CreateNotificationDialog
import com.mpieterse.stride.ui.layout.central.components.NotificationItem
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import com.mpieterse.stride.ui.layout.central.viewmodels.NotificationsViewModel

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateNotificationDialog by remember { mutableStateOf(false) }
    var showEditNotificationDialog by remember { mutableStateOf(false) }
    var notificationToEdit by remember { mutableStateOf<NotificationData?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    
    // Check notification permission status
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (NotificationPermissionHelper.shouldRequestPermission()) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                // For Android versions below 13, check if notifications are enabled
                NotificationPermissionHelper.areNotificationsEnabled(context)
            }
        )
    }
    
    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            // Permission granted, proceed to open dialog
            showCreateNotificationDialog = true
        } else {
            // Permission denied, show rationale
            showPermissionRationale = true
        }
    }
    
    // Handler for FAB click - request permission if needed
    val onFabClick = {
        if (NotificationPermissionHelper.shouldRequestPermission()) {
            // Android 13+ - check permission first
            if (hasNotificationPermission) {
                showCreateNotificationDialog = true
            } else {
                // Request permission
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android < 13 - notifications are enabled by default, just open dialog
            showCreateNotificationDialog = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (no refresh button)
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
                    settings = state.settings,
                    onSettingsChange = { viewModel.updateSettings(it) }
                )
                
                // Show permission warning if permission not granted (Android 13+)
                if (NotificationPermissionHelper.shouldRequestPermission() && !hasNotificationPermission) {
                    PermissionWarningCard(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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

                    if (state.loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading notifications...")
                        }
                    } else if (state.notifications.isEmpty()) {
                        EmptyNotificationsState(
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = state.notifications,
                                key = { it.id }
                            ) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onToggleEnabled = { enabled ->
                                        viewModel.toggleNotificationEnabled(notification.id, enabled)
                                    },
                                    onEdit = {
                                        notificationToEdit = notification
                                        showEditNotificationDialog = true
                                    },
                                    onDelete = {
                                        viewModel.deleteNotification(notification.id)
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
            onClick = onFabClick,
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
    
    // Permission rationale dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = {
                Text(
                    text = "Notification Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "To receive habit reminders, please enable notification permission in your device settings.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPermissionRationale = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Create Notification Dialog
    CreateNotificationDialog(
        isVisible = showCreateNotificationDialog,
        onDismiss = { showCreateNotificationDialog = false },
        onConfirm = { newNotification ->
            viewModel.addNotification(newNotification)
            showCreateNotificationDialog = false
        },
        availableHabits = state.habits
    )

    // Edit Notification Dialog
    CreateNotificationDialog(
        isVisible = showEditNotificationDialog,
        onDismiss = {
            showEditNotificationDialog = false
            notificationToEdit = null
        },
        onConfirm = { updatedNotification ->
            viewModel.updateNotification(updatedNotification)
            showEditNotificationDialog = false
            notificationToEdit = null
        },
        availableHabits = state.habits,
        initialData = notificationToEdit
    )
}

@Composable
private fun NotificationSettingsCard(
    settings: NotificationSettings,
    onSettingsChange: (NotificationSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var enableNotifications by remember { mutableStateOf(settings.globalNotificationsEnabled) }
    var soundEnabled by remember { mutableStateOf(settings.defaultSoundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(settings.defaultVibrationEnabled) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Global Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black
            )

            // Enable Notifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        enableNotifications = !enableNotifications
                        onSettingsChange(settings.copy(globalNotificationsEnabled = enableNotifications))
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Notifications", color = Color.Black)
                Switch(
                    checked = enableNotifications,
                    onCheckedChange = {
                        enableNotifications = it
                        onSettingsChange(settings.copy(globalNotificationsEnabled = it))
                    }
                )
            }

            // Default Sound
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        soundEnabled = !soundEnabled
                        onSettingsChange(settings.copy(defaultSoundEnabled = soundEnabled))
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Default Sound", color = Color.Black)
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        onSettingsChange(settings.copy(defaultSoundEnabled = it))
                    }
                )
            }

            // Default Vibration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        vibrationEnabled = !vibrationEnabled
                        onSettingsChange(settings.copy(defaultVibrationEnabled = vibrationEnabled))
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Default Vibration", color = Color.Black)
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = {
                        vibrationEnabled = it
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
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

@Composable
private fun PermissionWarningCard(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_bell),
                contentDescription = null,
                tint = Color(0xFFFF9500),
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Notification Permission Required",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Black
                )
                Text(
                    text = "Enable notifications to receive habit reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
