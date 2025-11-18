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

/**
 * Firebase Cloud Messaging service for handling push notifications.
 * 
 * This service receives push notifications from Firebase and displays them
 * to the user, even when the app is in the background.
 */
@AndroidEntryPoint
class StrideFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "StrideFCMService"
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    @Inject
    lateinit var fcmTokenManager: FcmTokenManager
    
    override fun onCreate() {
        super.onCreate()
        // Create notification channels when service is created
        NotificationChannelManager.createChannels(this)
        Clogger.d(TAG, "FirebaseMessagingService created")
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
        Clogger.d(TAG, "FCM token refreshed: ${token.take(20)}...")
        
        // Update token on server in background
        CoroutineScope(Dispatchers.IO).launch {
            fcmTokenManager.updateToken(token)
        }
    }
    
    /**
     * Handles data-only messages (no notification payload).
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Stride"
        val body = data["body"] ?: data["message"] ?: "You have a new notification"
        
        showNotification(title, body, data)
    }
    
    /**
     * Displays a local notification.
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
            val notificationBuilder = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.xic_uic_outline_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            
            // Check if notifications are enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!notificationManager.areNotificationsEnabled()) {
                    Clogger.w(TAG, "Notifications are disabled by user")
                    return
                }
            }
            
            // Generate unique notification ID
            val notificationId = NOTIFICATION_ID_BASE + (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            notificationManager.notify(notificationId, notificationBuilder.build())
            
            Clogger.d(TAG, "Notification displayed with ID: $notificationId")
        } catch (e: Exception) {
            Clogger.e(TAG, "Error displaying notification", e)
        }
    }
}

