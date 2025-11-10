package com.mpieterse.stride.data.dto.notifications

/**
 * DTOs for FCM token registration and notification-related API calls.
 */
data class FcmTokenRequest(val token: String, val deviceId: String? = null) //This data class represents FCM token registration request data using Kotlin data classes (Kotlin Foundation, 2024).

data class FcmTokenResponse(val success: Boolean, val message: String?) //This data class represents FCM token registration response data using Kotlin data classes (Kotlin Foundation, 2024).

