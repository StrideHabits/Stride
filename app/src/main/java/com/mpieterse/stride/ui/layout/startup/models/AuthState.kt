package com.mpieterse.stride.ui.layout.startup.models

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Locked : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}