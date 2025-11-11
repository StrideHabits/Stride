package com.mpieterse.stride.core

import android.app.Application
import androidx.work.Configuration
import com.mpieterse.stride.core.notifications.NotificationChannelManager
import com.mpieterse.stride.core.services.GlobalAuthenticationListener
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LocalApplication :
    Application(),
    androidx.work.Configuration.Provider {

    companion object { const val TAG = "LocalApplication" }

    @Inject lateinit var authenticationListener: GlobalAuthenticationListener
    @Inject lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Clogger.i(TAG, "Application initialized successfully")
        
        // Initialize notification channels
        NotificationChannelManager.createChannels(this)
        
        authenticationListener.listen()
        com.mpieterse.stride.workers.PullWorker.schedulePeriodic(this)
    }

    // WorkManager 2.9+: property override, not function
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
