package com.mpieterse.stride.core.permissions

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Manager for handling notification permissions.
 * 
 * On Android 13+ (API 33+), POST_NOTIFICATIONS permission must be requested at runtime.
 * This manager provides utilities to check and request notification permissions.
 */
object NotificationPermissionManager {
    
    /**
     * Checks if notifications are enabled for the app.
     * 
     * @param context The application context
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // On Android 13+, check if permission is granted
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            // On older versions, notifications are enabled by default
            true
        }
    }
    
    /**
     * Checks if notification permission needs to be requested.
     * 
     * @param context The application context
     * @return true if permission needs to be requested (Android 13+ and not granted), false otherwise
     */
    fun shouldRequestPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areNotificationsEnabled(context)
    }
}

