package com.mpieterse.stride.core.net

import okio.IOException

// core networking result wrapper
sealed class ApiResult<out T> {
    data class Ok<T>(val data: T): ApiResult<T>()
    data class Err(val code: Int?, val message: String): ApiResult<Nothing>()
}

/**
 * Safely extracts error message from response body.
 * Note: In safeCall, we only read errorBody once, so using string() is safe.
 * This helper just provides better error handling.
 * Made public because it's used by inline function safeCall.
 */
fun retrofit2.Response<*>.safeErrorBodyString(): String {
    return try {
        errorBody()?.string() ?: "Unknown error"
    } catch (e: IOException) {
        "Failed to read error message"
    } catch (e: Exception) {
        "Unknown error"
    }
}

suspend inline fun <T> safeCall(crossinline block: suspend () -> retrofit2.Response<T>): ApiResult<T> = //This function provides safe API call handling with error management for Retrofit responses (Square Inc., 2024).
    try {
        val res = block()
        val body = res.body()
        if (res.isSuccessful && body != null) ApiResult.Ok(body)
        else ApiResult.Err(res.code(), res.safeErrorBodyString())
    } catch (e: Exception) {
        ApiResult.Err(null, e.message ?: "Network error")
    }

/**
 * Extension functions for consistent error handling across the app.
 * Reduces code duplication and ensures consistent error detection logic.
 */

/**
 * Checks if the error is an authentication error (401 or 403).
 */
fun ApiResult.Err.isAuthError(): Boolean {
    return code != null && (code == 401 || code == 403)
}

/**
 * Checks if the error is a network error (timeout, connection issues, or null code).
 */
fun ApiResult.Err.isNetworkError(): Boolean {
    if (code == null) return true // Null code indicates network exception
    val msg = message.lowercase()
    return msg.contains("timeout", ignoreCase = true) ||
           msg.contains("network", ignoreCase = true) ||
           msg.contains("connection", ignoreCase = true) ||
           msg.contains("unreachable", ignoreCase = true) ||
           msg.contains("socket", ignoreCase = true)
}

/**
 * Checks if the error is a server error (500-599 range).
 */
fun ApiResult.Err.isServerError(): Boolean {
    return code != null && code in 500..599
}

/**
 * Checks if the error is a client error (400-499 range, excluding auth errors).
 */
fun ApiResult.Err.isClientError(): Boolean {
    return code != null && code in 400..499 && !isAuthError()
}

/**
 * Gets a user-friendly error message based on the error type.
 */
fun ApiResult.Err.getUserMessage(defaultMessage: String = "An error occurred. Please try again."): String {
    return when {
        isAuthError() -> message.ifBlank { "Authentication failed. Please sign in again." }
        isNetworkError() -> "Network error. Please check your internet connection and try again."
        isServerError() -> "Server error. Please try again later."
        code == 404 -> message.ifBlank { "Resource not found. Please check and try again." }
        code == 400 -> message.ifBlank { "Invalid request. Please check your input and try again." }
        else -> message.ifBlank { defaultMessage }
    }
}
