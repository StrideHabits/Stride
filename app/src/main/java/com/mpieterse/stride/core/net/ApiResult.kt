package com.mpieterse.stride.core.net

// core networking result wrapper
sealed class ApiResult<out T> {
    data class Ok<T>(val data: T): ApiResult<T>()
    data class Err(val code: Int?, val message: String): ApiResult<Nothing>()
}
suspend inline fun <T> safeCall(crossinline block: suspend () -> retrofit2.Response<T>): ApiResult<T> = //This function provides safe API call handling with error management for Retrofit responses (Square Inc., 2024).
    try {
        val res = block()
        val body = res.body()
        if (res.isSuccessful && body != null) ApiResult.Ok(body)
        else ApiResult.Err(res.code(), res.errorBody()?.string() ?: "Unknown error")
    } catch (e: Exception) {
        // Preserve exception type information for better error handling
        val errorMessage = when {
            e is java.net.SocketTimeoutException -> "timeout: ${e.message ?: "Request timed out"}"
            e is java.net.UnknownHostException -> "network: ${e.message ?: "Unable to reach server"}"
            e is java.io.IOException -> "network: ${e.message ?: "Network error"}"
            else -> e.message ?: "Network error"
        }
        ApiResult.Err(null, errorMessage)
    }
