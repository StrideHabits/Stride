package com.mpieterse.stride.core.services

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.remote.SummitApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Firebase Cloud Messaging (FCM) tokens.
 * 
 * Handles token retrieval and synchronization with the backend server.
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
     * Gets the current FCM token.
     * 
     * @return The FCM token, or null if unavailable
     */
    suspend fun getToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Clogger.d(TAG, "FCM token retrieved: ${token.take(20)}...")
            token
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    /**
     * Updates the FCM token on the server.
     * 
     * @param token The FCM token to send to the server
     */
    suspend fun updateToken(token: String) {
        try {
            Clogger.d(TAG, "Updating FCM token on server: ${token.take(20)}...")
            val request = com.mpieterse.stride.data.dto.auth.FcmTokenUpdateRequest(token = token)
            val response = apiService.updateFcmToken(request)
            if (response.isSuccessful) {
                Clogger.d(TAG, "FCM token updated successfully on server")
            } else {
                Clogger.w(TAG, "Failed to update FCM token on server: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to update FCM token on server", e)
            // Don't throw - token update failure shouldn't break the app
        }
    }
    
    /**
     * Initializes FCM token and sends it to the server.
     * Should be called after user authentication.
     */
    suspend fun initializeToken() {
        val token = getToken()
        if (token != null) {
            updateToken(token)
        }
    }
}

