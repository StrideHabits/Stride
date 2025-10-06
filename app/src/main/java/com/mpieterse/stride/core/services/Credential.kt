package com.mpieterse.stride.core.services

data class Credential(
    val email: String,
    val token: String,
) {
    companion object {
        const val TAG = "Credential"
    }
}