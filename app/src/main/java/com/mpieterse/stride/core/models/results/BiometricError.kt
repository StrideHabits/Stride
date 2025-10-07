package com.mpieterse.stride.core.models.results

sealed class BiometricError : GenericError {
    object NoSupport : BiometricError()
    object RateLimit : BiometricError()
    object Dismissed : BiometricError()
    object Failed : BiometricError()
    data class Exception(
        val code: Int,
        val text: String
    ) : BiometricError()
}