package com.mpieterse.stride.core

import android.app.Application
import com.mpieterse.stride.core.notifications.NotificationChannelManager
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
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


// --- Lifecycle


    override fun onCreate() {
        super.onCreate()
        Clogger.i(
            TAG, "Application initialized successfully"
        )

        // Initialize notification channels
        NotificationChannelManager.createChannels(this)

        authenticationListener.listen()
        
        // Register FCM token with backend (async, non-blocking)
        applicationScope.launch {
            fcmTokenManager.registerTokenWithBackend()
        }
    }
}