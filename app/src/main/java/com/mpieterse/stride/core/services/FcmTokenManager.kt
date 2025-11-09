package com.mpieterse.stride.core.services

import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.dto.notifications.FcmTokenRequest
import com.mpieterse.stride.data.remote.SummitApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages FCM token registration and synchronization with the backend.
 */
@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: SummitApiService
) {
    
    companion object {
        private const val TAG = "FcmTokenManager"
    }
    
    /**
     * Gets the current FCM token and registers it with the backend.
     */
    suspend fun registerTokenWithBackend(): ApiResult<Boolean> {
        return try {
            // Get FCM token
            val token = FirebaseMessaging.getInstance().token.await()
            Clogger.d(TAG, "FCM Token retrieved: ${token.take(20)}...")
            
            // Get device ID (Android ID)
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            
            // Register token with backend
            val request = FcmTokenRequest(token = token, deviceId = deviceId)
            val response = apiService.registerFcmToken(request)
            
            if (response.isSuccessful) {
                Clogger.d(TAG, "FCM token registered successfully with backend")
                ApiResult.Ok(true)
            } else {
                Clogger.w(TAG, "Failed to register FCM token: ${response.code()} - ${response.message()}")
                ApiResult.Err(response.code(), "Failed to register FCM token: ${response.message()}")
            }
        } catch (e: Exception) {
            Clogger.e(TAG, "Error registering FCM token", e)
            ApiResult.Err(null, "Error registering FCM token: ${e.message}")
        }
    }
    
    /**
     * Gets the current FCM token without registering it.
     */
    suspend fun getCurrentToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Clogger.e(TAG, "Error getting FCM token", e)
            null
        }
    }
}

