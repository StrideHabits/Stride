package com.mpieterse.stride.core.permissions

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Helper class for checking notification permission status.
 * 
 * For Android 13+ (API 33+), POST_NOTIFICATIONS permission is required.
 * For older versions, notifications are enabled by default.
 */
object NotificationPermissionHelper {
    
    /**
     * Checks if notifications are enabled for this app.
     * 
     * @param context The application context
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            // For Android versions below API 24, notifications are always enabled
            true
        }
    }
    
    /**
     * Checks if the app needs to request POST_NOTIFICATIONS permission.
     * 
     * @return true if permission request is needed (Android 13+), false otherwise
     */
    fun shouldRequestPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}

