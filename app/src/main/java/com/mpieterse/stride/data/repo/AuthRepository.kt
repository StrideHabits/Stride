package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.net.safeCall
import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.auth.*
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.remote.SummitApiService
import javax.inject.Inject

/**
 * Repository class implementing the Repository Design Pattern in Kotlin.
 *
 * This class acts as a clean data access layer between the ViewModel and data sources
 * (e.g., local database, remote API, or in-memory cache). It abstracts data operations
 * to ensure separation of concerns, maintainability, and scalability across the app.
 *
 * @see <a href="https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40">
 *      App Dev Insights (2024). Repository Design Pattern in Kotlin.</a>
 *      [Accessed 6 Oct. 2025].
 */


class AuthRepository @Inject constructor(
    private val api: SummitApiService,
    private val tokens: TokenStore
) {
    suspend fun register(name: String, email: String, pass: String) = //This method registers a new user through the API using the Repository pattern (App Dev Insights, 2024).
        safeCall { api.register(RegisterRequest(name, email, pass)) }

    suspend fun login(email: String, pass: String) = //This method authenticates a user through the API and stores the token using the Repository pattern (App Dev Insights, 2024).
        safeCall { api.login(LoginRequest(email, pass)) }.also {
            if (it is ApiResult.Ok) tokens.set(it.data.token)
        }

    suspend fun logout() = tokens.clear() //This method clears stored authentication tokens using the Repository pattern (App Dev Insights, 2024).
}
