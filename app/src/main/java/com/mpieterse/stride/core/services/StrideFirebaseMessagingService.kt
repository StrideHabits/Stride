package com.mpieterse.stride.core.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mpieterse.stride.R
import com.mpieterse.stride.core.notifications.NotificationChannelManager
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.ui.layout.central.roots.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

/**
 * Firebase Cloud Messaging service for handling push notifications.
 * 
 * This service receives push notifications from Firebase and displays them
 * to the user, even when the app is in the background or closed.
 */
@AndroidEntryPoint
class StrideFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "StrideFCMService"
        private const val NOTIFICATION_ID_BASE = 1000
        private val notificationIdCounter = AtomicInteger(NOTIFICATION_ID_BASE)
    }
    
    @Inject
    lateinit var fcmTokenManager: FcmTokenManager
    
    override fun onCreate() {
        super.onCreate()
        // Create notification channels when service is created
        NotificationChannelManager.createChannels(this)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Clogger.d(TAG, "Received FCM message from: ${remoteMessage.from}")
        Clogger.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Clogger.d(TAG, "Message Type: ${if (remoteMessage.notification != null) "Notification" else "Data"}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Clogger.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Clogger.d(TAG, "Message Notification - Title: ${notification.title}, Body: ${notification.body}")
            showNotification(
                title = notification.title ?: "Stride",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
        
        // If no notification payload but has data, show notification from data
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            Clogger.d(TAG, "No notification payload, creating notification from data")
            handleDataMessage(remoteMessage.data)
        }
    }
    
    override fun onNewToken(token: String) {
        Clogger.i(TAG, "FCM token refreshed: ${token.take(20)}... (length: ${token.length})")
        // Send token to backend
        sendRegistrationToServer(token)
    }
    
    /**
     * Handles data-only messages (when app is in foreground).
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Habit Reminder"
        val body = data["body"] ?: data["message"] ?: "You have a habit reminder"
        val habitId = data["habitId"]
        val habitName = data["habitName"] ?: "Habit"
        
        showNotification(
            title = title,
            body = body,
            data = data
        )
    }
    
    /**
     * Displays a notification to the user.
     */
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        try {
            Clogger.d(TAG, "Displaying notification - Title: $title, Body: $body")
            
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // Add any extra data if needed
                data.forEach { (key, value) ->
                    putExtra(key, value)
                }
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_HABIT_REMINDERS)
                .setSmallIcon(R.drawable.xic_uic_outline_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = notificationIdCounter.getAndUpdate { current ->
                if (current == Int.MAX_VALUE) NOTIFICATION_ID_BASE else current + 1
            }
            
            // Check if notifications are enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!notificationManager.areNotificationsEnabled()) {
                    Clogger.w(TAG, "Notifications are disabled by user")
                    return
                }
            }
            
            notificationManager.notify(notificationId, notificationBuilder.build())
            Clogger.d(TAG, "Notification displayed successfully with ID: $notificationId")
        } catch (e: Exception) {
            Clogger.e(TAG, "Error displaying notification", e)
        }
    }
    
    /**
     * Sends FCM registration token to the backend server.
     */
    private fun sendRegistrationToServer(token: String) {
        // Use FcmTokenManager to register token with backend
        // This is called asynchronously, so we don't block the service
        CoroutineScope(Dispatchers.IO).launch {
            fcmTokenManager.registerTokenWithBackend()
        }
    }
}

