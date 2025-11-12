package com.mpieterse.stride.data.remote

import android.content.Context
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.local.NotificationsStore
import com.mpieterse.stride.core.services.SessionManager
import com.google.gson.GsonBuilder
import com.mpieterse.stride.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dependency Injection (DI) module for providing application-level dependencies.
 *
 * This module uses Hilt to manage and inject dependencies such as repositories,
 * services, and data sources throughout the app. Centralizing object creation
 * improves testability, scalability, and adherence to clean architecture principles.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection">
 * Android Developers (2025). Dependency injection in Android.</a>
 * [Accessed 6 Oct. 2025].
 */


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun tokenStore(@ApplicationContext c: Context) = TokenStore(c)
    @Provides @Singleton fun notificationsStore(@ApplicationContext c: Context) = NotificationsStore(c)

    @Provides @Singleton
    fun authInterceptor(
        store: TokenStore,
        sessionManager: SessionManager
    ) = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = runBlocking { store.tokenFlow.first() }
        val requestWithAuth = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val path = originalRequest.url.encodedPath
        val isAuthEndpoint = path.endsWith("/api/users/login") || path.endsWith("/api/users/register")

        var response = try {
            chain.proceed(requestWithAuth)
        } catch (e: Exception) {
            // Network errors (timeouts, connection issues) should not trigger logout
            // Only re-throw the exception - don't attempt session restoration on network failures
            throw e
        }

        // Handle auth errors (401/403) and potential auth-related 500 errors
        if (!isAuthEndpoint && (response.code == 401 || response.code == 403 || response.code == 500)) {
            // Attempt session restoration for auth errors and 500 (which might indicate expired token)
            val originalToken = token
            val restoreResult = sessionManager.tryRestoreSession()
            when (restoreResult) {
                SessionManager.SessionRestoreResult.RESTORED -> {
                    // Session restored successfully - retry the original request with new token
                    response.close()
                    val refreshedToken = runBlocking { store.tokenFlow.first() }
                    val retryRequest = originalRequest.newBuilder().apply {
                        if (!refreshedToken.isNullOrBlank()) {
                            header("Authorization", "Bearer $refreshedToken")
                        } else {
                            removeHeader("Authorization")
                        }
                    }.build()
                    response = chain.proceed(retryRequest)
                    // If retry still fails with 500, it's a real server error, not auth-related
                }
                SessionManager.SessionRestoreResult.ALREADY_RESTORING -> {
                    // Another request is already restoring the session - wait for it to complete
                    val restorationSucceeded = sessionManager.waitForRestoration(originalToken)
                    if (restorationSucceeded) {
                        // Restoration completed successfully - retry with new token
                        response.close()
                        val refreshedToken = runBlocking { store.tokenFlow.first() }
                        val retryRequest = originalRequest.newBuilder().apply {
                            if (!refreshedToken.isNullOrBlank()) {
                                header("Authorization", "Bearer $refreshedToken")
                            } else {
                                removeHeader("Authorization")
                            }
                        }.build()
                        response = chain.proceed(retryRequest)
                    }
                    // If restoration failed or timed out, return the original error response
                    // (don't close it - let the caller handle it)
                }
                SessionManager.SessionRestoreResult.INVALID_CREDENTIALS,
                SessionManager.SessionRestoreResult.NO_CREDENTIALS -> {
                    // Only logout if we definitively know credentials are invalid or missing
                    sessionManager.forceLogout()
                }
                SessionManager.SessionRestoreResult.NETWORK_ERROR,
                SessionManager.SessionRestoreResult.SERVER_ERROR -> {
                    // For 500 errors, if session restoration failed, it might be a real server error
                    // Don't logout - just return the error response
                    // For 401/403, keep the user logged in and allow retries
                }
            }
        }

        response
    }

    @Provides @Singleton
    fun okHttp(auth: Interceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logger) // Interceptor is correctly added here
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(20))
            .writeTimeout(java.time.Duration.ofSeconds(20))
            .build()
    }

    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(client)
            .build()

    @Provides @Singleton
    fun api(retrofit: Retrofit): SummitApiService =
        retrofit.create(SummitApiService::class.java)

    /**
     * Provides a Retrofit instance without the auth interceptor for re-authentication.
     * This breaks the dependency cycle: SessionManager -> SummitApiService -> Retrofit -> OkHttpClient -> Interceptor -> SessionManager
     */
    @Provides @Singleton @Named("reauth")
    fun reauthRetrofit(): Retrofit {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(20))
            .writeTimeout(java.time.Duration.ofSeconds(20))
            .build()
        
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(client)
            .build()
    }

    /**
     * Provides a SummitApiService instance without the auth interceptor for re-authentication.
     */
    @Provides @Singleton @Named("reauth")
    fun reauthApi(@Named("reauth") reauthRetrofit: Retrofit): SummitApiService =
        reauthRetrofit.create(SummitApiService::class.java)
}