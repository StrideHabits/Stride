package com.mpieterse.stride.core

import android.app.Application
import android.os.Build
import com.mpieterse.stride.core.notifications.NotificationChannelManager
import com.mpieterse.stride.core.permissions.NotificationPermissionManager
import com.mpieterse.stride.core.services.HabitSyncManager
import com.mpieterse.stride.core.services.FcmTokenManager
import com.mpieterse.stride.core.services.GlobalAuthenticationListener
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LocalApplication : Application() {
    companion object {
        const val TAG = "LocalApplication"
    }

    @Inject
    lateinit var authenticationListener: GlobalAuthenticationListener
    
    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    @Inject
    lateinit var habitSyncManager: HabitSyncManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


// --- Lifecycle


    override fun onCreate() {
        super.onCreate()
        Clogger.i(
            TAG, "Application initialized successfully"
        )

        // Initialize notification channels (required for Android 8.0+)
        NotificationChannelManager.createChannels(this)
        Clogger.d(TAG, "Notification channels created")

        // Check notification permission status (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = NotificationPermissionManager.areNotificationsEnabled(this)
            Clogger.d(TAG, "Notification permission status: $hasPermission")
            if (!hasPermission) {
                Clogger.w(TAG, "Notification permission not granted - notifications may not work")
            }
        }

        authenticationListener.listen()

        habitSyncManager.start()
        
        // Register FCM token with backend (async, non-blocking)
        applicationScope.launch {
            try {
                Clogger.d(TAG, "Attempting to register FCM token with backend")
                val result = fcmTokenManager.registerTokenWithBackend()
                when (result) {
                    is com.mpieterse.stride.core.net.ApiResult.Ok -> {
                        Clogger.i(TAG, "FCM token registered successfully with backend")
                    }
                    is com.mpieterse.stride.core.net.ApiResult.Err -> {
                        Clogger.w(TAG, "Failed to register FCM token: ${result.code} - ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Clogger.e(TAG, "Error registering FCM token", e)
            }
        }
    }
}