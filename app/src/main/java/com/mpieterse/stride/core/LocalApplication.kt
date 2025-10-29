package com.mpieterse.stride.core

import android.app.Application
import com.mpieterse.stride.core.services.GlobalAuthenticationListener
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LocalApplication : Application() {
    companion object {
        const val TAG = "LocalApplication"
    }

    @Inject
    lateinit var authenticationListener: GlobalAuthenticationListener


// --- Lifecycle


    override fun onCreate() {
        super.onCreate()
        Clogger.i(
            TAG, "Application initialized successfully"
        )

        authenticationListener.listen()
        com.mpieterse.stride.workers.PullWorker.schedulePeriodic(this) // sync workers
    }
}