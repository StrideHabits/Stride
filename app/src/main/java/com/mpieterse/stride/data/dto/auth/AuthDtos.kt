// data/dto/auth/AuthDtos.kt
package com.mpieterse.stride.data.dto.auth

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class RegisterResponse(val id: String, val email: String)
data class AuthResponse(val id: String, val email: String, val token: String)
