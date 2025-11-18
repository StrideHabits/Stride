package com.mpieterse.stride.core.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mpieterse.stride.R
import com.mpieterse.stride.core.notifications.NotificationChannelManager
import com.mpieterse.stride.core.notifications.NotificationTemplateEngine
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import com.mpieterse.stride.ui.layout.central.roots.HomeActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling local notifications using WorkManager.
 * 
 * This service handles scheduling, updating, and canceling local notifications
 * based on NotificationData and NotificationSettings.
 */
@Singleton
class NotificationSchedulerService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    
    companion object {
        private const val TAG = "NotificationScheduler"
        private const val WORK_NAME_PREFIX = "habit_notification_"
        private const val NOTIFICATION_ID_BASE = 2000
    }
    
    /**
     * Schedules a notification based on NotificationData.
     */
    fun scheduleNotification(notification: NotificationData, settings: NotificationSettings) {
        if (!notification.isEnabled || !settings.globalNotificationsEnabled) {
            Clogger.d(TAG, "Notification ${notification.id} (${notification.habitName}) is disabled, skipping scheduling")
            return
        }
        
        Clogger.d(TAG, "Scheduling notification ${notification.id} for habit: ${notification.habitName}")
        Clogger.d(TAG, "Notification time: ${notification.time}, Days: ${notification.daysOfWeek}")
        
        // Cancel existing work for this notification
        cancelNotification(notification.id)
        
        // Schedule notification for each day of the week
        notification.daysOfWeek.forEach { dayOfWeek ->
            scheduleNotificationForDay(notification, dayOfWeek, settings)
        }
        
        Clogger.i(TAG, "Successfully scheduled notification ${notification.id} for ${notification.daysOfWeek.size} day(s)")
    }
    
    /**
     * Schedules a notification for a specific day of the week.
     */
    private fun scheduleNotificationForDay(
        notification: NotificationData,
        dayOfWeek: Int,
        settings: NotificationSettings
    ) {
        val template = NotificationTemplateEngine.applyTemplate(notification)
        
        // Convert day of week (1=Monday, 7=Sunday) to DayOfWeek enum
        val javaDayOfWeek = when (dayOfWeek) {
            1 -> DayOfWeek.MONDAY
            2 -> DayOfWeek.TUESDAY
            3 -> DayOfWeek.WEDNESDAY
            4 -> DayOfWeek.THURSDAY
            5 -> DayOfWeek.FRIDAY
            6 -> DayOfWeek.SATURDAY
            7 -> DayOfWeek.SUNDAY
            else -> return
        }
        
        // Calculate initial delay to next occurrence of this day/time
        val delay = calculateDelayToNextOccurrence(notification.time, javaDayOfWeek)
        
        // Create work request data
        val workData = Data.Builder()
            .putString("notification_id", notification.id)
            .putString("habit_name", notification.habitName)
            .putString("title", template.title)
            .putString("body", template.body)
            .putBoolean("sound_enabled", notification.soundEnabled && settings.defaultSoundEnabled)
            .putBoolean("vibration_enabled", notification.vibrationEnabled && settings.defaultVibrationEnabled)
            .putInt("day_of_week", dayOfWeek)
            .build()
        
        // Align the first periodic run to the intended next occurrence
        val periodicInitialDelay = delay.coerceAtLeast(0L)
        
        // Schedule the periodic work (weekly recurrence) anchored by the computed delay
        // WorkManager handles background execution automatically, even when app is killed
        // Minimum repeat interval for PeriodicWorkRequest is 15 minutes
        // We use 7 days for weekly recurrence
        val periodicRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            7,
            TimeUnit.DAYS
        )
            .setInputData(workData)
            .setInitialDelay(periodicInitialDelay, TimeUnit.MILLISECONDS)
            .addTag("${WORK_NAME_PREFIX}${notification.id}_$dayOfWeek")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false) // Allow even on low battery
                    .setRequiresCharging(false) // Allow even when not charging
                    .build()
            )
            .build()
        
        // Use unique work to replace existing periodic work
        workManager.enqueueUniquePeriodicWork(
            "${WORK_NAME_PREFIX}${notification.id}_$dayOfWeek",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
        
        Clogger.d(TAG, "Scheduled work for notification ${notification.id} on day $dayOfWeek")
    }
    
    /**
     * Calculates the delay in milliseconds until the next occurrence of the specified day and time.
     * 
     * This method ensures that the notification is scheduled for the next occurrence of the
     * specified day and time, even if that occurrence is later in the current week or next week.
     */
    private fun calculateDelayToNextOccurrence(time: LocalTime, dayOfWeek: DayOfWeek): Long {
        val now = Calendar.getInstance()
        val targetDayOfWeek = dayOfWeekToCalendarDay(dayOfWeek)
        
        val targetCalendar = Calendar.getInstance().apply {
            // Set to the target day and time
            set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If the target time has already passed this week, schedule for next week
        if (targetCalendar.before(now) || targetCalendar.timeInMillis <= now.timeInMillis) {
            targetCalendar.add(Calendar.DAY_OF_WEEK, 7)
        }
        
        val delay = targetCalendar.timeInMillis - now.timeInMillis
        return delay.coerceAtLeast(0)
    }
    
    /**
     * Converts Java DayOfWeek to Calendar day constant.
     */
    private fun dayOfWeekToCalendarDay(dayOfWeek: DayOfWeek): Int {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
        }
    }
    
    /**
     * Cancels a scheduled notification.
     */
    fun cancelNotification(notificationId: String) {
        Clogger.d(TAG, "Cancelling notification $notificationId")
        // Cancel all work items for this notification (all days)
        for (day in 1..7) {
            val tag = "${WORK_NAME_PREFIX}${notificationId}_$day"
            workManager.cancelAllWorkByTag(tag)
            // Cancel unique periodic work
            workManager.cancelUniqueWork("${WORK_NAME_PREFIX}${notificationId}_$day")
        }
        Clogger.d(TAG, "Cancelled all work for notification $notificationId")
    }
    
    /**
     * Reschedules all notifications (useful when settings change).
     */
    fun rescheduleAllNotifications(
        notifications: List<NotificationData>,
        settings: NotificationSettings
    ) {
        // Cancel all existing work for notifications
        notifications.forEach { notification ->
            cancelNotification(notification.id)
        }
        
        // Schedule all enabled notifications
        notifications.forEach { notification ->
            if (notification.isEnabled && settings.globalNotificationsEnabled) {
                scheduleNotification(notification, settings)
            }
        }
        
        Clogger.d(TAG, "Rescheduled ${notifications.size} notification(s)")
    }
}

/**
 * Worker class that displays the notification when triggered.
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val notificationId = inputData.getString("notification_id") ?: return Result.failure()
            val habitName = inputData.getString("habit_name") ?: "Habit Reminder"
            val title = inputData.getString("title") ?: habitName
            val body = inputData.getString("body") ?: "Time to work on your habit!"
            val soundEnabled = inputData.getBoolean("sound_enabled", true)
            val vibrationEnabled = inputData.getBoolean("vibration_enabled", true)
            
            Clogger.d("NotificationWorker", "Executing notification work for: $habitName")
            Clogger.d("NotificationWorker", "Notification ID: $notificationId, Title: $title, Body: $body")
            
            // Display notification
            displayNotification(notificationId, title, body, soundEnabled, vibrationEnabled)
            
            Clogger.d("NotificationWorker", "Notification work completed successfully")
            Result.success()
        } catch (e: Exception) {
            Clogger.e("NotificationWorker", "Error executing notification work", e)
            Result.failure()
        }
    }
    
    private fun displayNotification(
        notificationId: String,
        title: String,
        body: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val intent = Intent(applicationContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notificationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            NotificationChannelManager.CHANNEL_HABIT_REMINDERS
        )
            .setSmallIcon(R.drawable.xic_uic_outline_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(
                (if (soundEnabled) NotificationCompat.DEFAULT_SOUND else 0) or
                (if (vibrationEnabled) NotificationCompat.DEFAULT_VIBRATE else 0)
            )
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notificationIdInt = NOTIFICATION_ID_BASE + notificationId.hashCode()
        notificationManager.notify(notificationIdInt, notificationBuilder.build())
    }
    
    companion object {
        private const val NOTIFICATION_ID_BASE = 2000
    }
}

