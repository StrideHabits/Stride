package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.net.safeCall
import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.auth.*
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.remote.SummitApiService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: SummitApiService,
    private val tokens: TokenStore
) {
    suspend fun register(name: String, email: String, pass: String): ApiResult<AuthResponse> =
        safeCall { api.register(RegisterRequest(name, email, pass)) }.also {
            if (it is ApiResult.Ok) tokens.set(it.data.token)
        }

    suspend fun login(email: String, pass: String): ApiResult<AuthResponse> =
        safeCall { api.login(LoginRequest(email, pass)) }.also {
            if (it is ApiResult.Ok) tokens.set(it.data.token)
        }

    suspend fun logout() = tokens.clear()
}
