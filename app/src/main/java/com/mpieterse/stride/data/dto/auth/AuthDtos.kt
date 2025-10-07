package com.mpieterse.stride.data.dto.auth

data class RegisterRequest(val name: String, val email: String, val password: String) //This data class represents user registration request data using Kotlin data classes (Kotlin Foundation, 2024).
data class LoginRequest(val email: String, val password: String) //This data class represents user login request data using Kotlin data classes (Kotlin Foundation, 2024).
data class RegisterResponse(val id: String, val email: String) //This data class represents user registration response data using Kotlin data classes (Kotlin Foundation, 2024).
data class AuthResponse(val id: String, val email: String, val token: String) //This data class represents authentication response data using Kotlin data classes (Kotlin Foundation, 2024).
