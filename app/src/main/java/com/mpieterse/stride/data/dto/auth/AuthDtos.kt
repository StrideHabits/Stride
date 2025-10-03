package com.mpieterse.stride.data.dto.auth
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val id: String, val name: String, val token: String)
