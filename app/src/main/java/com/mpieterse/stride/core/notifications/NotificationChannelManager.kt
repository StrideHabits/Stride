package com.mpieterse.stride.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.mpieterse.stride.core.utils.Clogger

/**
 * Manages notification channels for the app.
 * 
 * Notification channels are required on Android 8.0 (API level 26) and higher.
 * This manager creates and configures all notification channels used by the app.
 */
object NotificationChannelManager {
    private const val TAG = "NotificationChannelManager"
    
    // Channel IDs
    const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
    const val CHANNEL_GENERAL = "general_notifications"
    const val CHANNEL_QUIET_HOURS = "quiet_hours"
    
    // Channel Names
    private const val CHANNEL_NAME_HABIT_REMINDERS = "Habit Reminders"
    private const val CHANNEL_NAME_GENERAL = "General Notifications"
    private const val CHANNEL_NAME_QUIET_HOURS = "Quiet Hours"
    
    // Channel Descriptions
    private const val CHANNEL_DESC_HABIT_REMINDERS = "Notifications for habit reminders and check-ins"
    private const val CHANNEL_DESC_GENERAL = "General app notifications"
    private const val CHANNEL_DESC_QUIET_HOURS = "Notifications during quiet hours (low priority)"
    
    /**
     * Creates all notification channels for the app.
     * Should be called during app initialization (e.g., in Application.onCreate).
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Habit Reminders Channel (High Importance)
            val habitRemindersChannel = NotificationChannel(
                CHANNEL_HABIT_REMINDERS,
                CHANNEL_NAME_HABIT_REMINDERS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_HABIT_REMINDERS
                enableVibration(true)
                enableLights(true)
            }
            
            // General Notifications Channel (Default Importance)
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_GENERAL
                enableVibration(true)
            }
            
            // Quiet Hours Channel (Low Importance)
            val quietHoursChannel = NotificationChannel(
                CHANNEL_QUIET_HOURS,
                CHANNEL_NAME_QUIET_HOURS,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC_QUIET_HOURS
                enableVibration(false)
            }
            
            // Create all channels
            notificationManager.createNotificationChannel(habitRemindersChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(quietHoursChannel)
            
            Clogger.d(TAG, "Notification channels created successfully")
        }
    }
    
    /**
     * Checks if notifications are enabled for a specific channel.
     */
    fun areNotificationsEnabled(context: Context, channelId: String): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                 (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                     .getNotificationChannel(channelId)?.importance != NotificationManager.IMPORTANCE_NONE)
    }
}

