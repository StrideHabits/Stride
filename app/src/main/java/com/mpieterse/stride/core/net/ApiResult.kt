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
        ApiResult.Err(null, e.message ?: "Network error")
    }
