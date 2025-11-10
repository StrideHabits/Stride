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
            Clogger.d(TAG, "Starting FCM token registration process")
            
            // Get FCM token
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNullOrBlank()) {
                Clogger.e(TAG, "FCM token is null or blank")
                return ApiResult.Err(null, "FCM token is null or blank")
            }
            
            Clogger.d(TAG, "FCM Token retrieved successfully: ${token.take(20)}... (length: ${token.length})")
            
            // Get device ID (Android ID)
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (deviceId.isNullOrBlank()) {
                Clogger.w(TAG, "Device ID is null or blank, using fallback")
            } else {
                Clogger.d(TAG, "Device ID retrieved: ${deviceId.take(10)}...")
            }
            
            // Register token with backend
            val request = FcmTokenRequest(token = token, deviceId = deviceId ?: "unknown")
            Clogger.d(TAG, "Sending FCM token registration request to backend")
            val response = apiService.registerFcmToken(request)
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Clogger.i(TAG, "FCM token registered successfully with backend. Response: ${responseBody?.toString() ?: "empty"}")
                ApiResult.Ok(true)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Clogger.w(TAG, "Failed to register FCM token: ${response.code()} - ${response.message()}. Error: $errorBody")
                ApiResult.Err(response.code(), "Failed to register FCM token: ${response.message()}")
            }
        } catch (e: Exception) {
            Clogger.e(TAG, "Error registering FCM token", e)
            ApiResult.Err(null, "Error registering FCM token: ${e.message ?: "Unknown error"}")
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

