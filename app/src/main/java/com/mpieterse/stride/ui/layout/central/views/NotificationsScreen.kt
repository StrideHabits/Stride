package com.mpieterse.stride.ui.layout.central.views

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Header (no refresh button)
                Text(
                    text = stringResource(R.string.notifications_habit_reminders),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.notifications_reminders_description),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
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
                    if (state.loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.notifications_loading))
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.xic_uic_outline_plus),
                contentDescription = stringResource(R.string.content_description_add_notification),
                modifier = Modifier.padding(4.dp)
            )
        }
    }
    
    // Permission rationale dialog
    AnimatedVisibility(
        visible = showPermissionRationale,
        enter = fadeIn(animationSpec = tween(durationMillis = TransitionConfig.NORMAL_DURATION)),
        exit = fadeOut(animationSpec = tween(durationMillis = TransitionConfig.FAST_DURATION))
    ) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = {
                Text(
                    text = stringResource(R.string.notifications_permission_required_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notifications_permission_required_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPermissionRationale = false }
                ) {
                    Text(stringResource(R.string.notifications_permission_ok))
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
        availableHabits = state.habits,
        isLoading = state.loading
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
        initialData = notificationToEdit,
        isLoading = state.loading
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.notifications_global_settings),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
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
                Text(stringResource(R.string.notifications_enable_notifications), color = MaterialTheme.colorScheme.onBackground)
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
                Text(stringResource(R.string.notifications_default_sound), color = MaterialTheme.colorScheme.onBackground)
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
                Text(stringResource(R.string.notifications_default_vibration), color = MaterialTheme.colorScheme.onBackground)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = stringResource(R.string.notifications_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.notifications_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.notifications_enable_permission_title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.notifications_enable_permission_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
