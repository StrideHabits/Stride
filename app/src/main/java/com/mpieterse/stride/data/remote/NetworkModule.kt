package com.mpieterse.stride.data.remote

import android.content.Context
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.local.NotificationsStore
import com.mpieterse.stride.core.services.SessionManager
import com.mpieterse.stride.core.utils.Clogger
import com.google.gson.GsonBuilder
import com.mpieterse.stride.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

    /**
     * Thread-safe token cache to avoid blocking in OkHttp interceptor.
     * The token is cached in memory and updated asynchronously from the flow.
     */
    private val tokenCache = java.util.concurrent.atomic.AtomicReference<String?>()
    private val tokenCollectorStarted = java.util.concurrent.atomic.AtomicBoolean(false)
    private val tokenInitializing = java.util.concurrent.atomic.AtomicBoolean(false)
    private val tokenScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Creates a logging interceptor only in debug builds.
     * Returns null in release builds since HttpLoggingInterceptor is only available via debugImplementation.
     */
    private fun createLoggingInterceptor(): Interceptor? {
        if (!BuildConfig.DEBUG) return null
        
        return try {
            // Use reflection to access HttpLoggingInterceptor which is only available in debug builds
            val interceptorClass = Class.forName("okhttp3.logging.HttpLoggingInterceptor")
            val interceptor = interceptorClass.getDeclaredConstructor().newInstance()
            val levelEnum = Class.forName("okhttp3.logging.HttpLoggingInterceptor\$Level")
            val bodyLevel = levelEnum.getField("BODY").get(null)
            
            val setLevelMethod = interceptorClass.getMethod("setLevel", levelEnum)
            setLevelMethod.invoke(interceptor, bodyLevel)
            
            val redactHeaderMethod = interceptorClass.getMethod("redactHeader", String::class.java)
            redactHeaderMethod.invoke(interceptor, "Authorization")
            
            interceptor as Interceptor
        } catch (e: Exception) {
            // If reflection fails, return null (release build or class not available)
            null
        }
    }

    @Provides @Singleton
    fun authInterceptor(
        store: TokenStore,
        sessionManager: SessionManager
    ): Interceptor {
        fun ensureTokenCacheInitialized() {
            // Initialize token cache with concurrency guard to prevent duplicate blocking work
            if (tokenCache.get() == null && tokenInitializing.compareAndSet(false, true)) {
                try {
                    runBlocking {
                        val initialToken = withTimeout(500) {
                            store.tokenFlow.first()
                        }
                        tokenCache.compareAndSet(null, initialToken)
                    }
                } catch (e: Exception) {
                    // Token initialization failure should not crash requests
                    // Allow graceful degradation (unauthenticated request)
                    Clogger.e("NetworkModule", "Failed to initialize token cache", e)
                } finally {
                    tokenInitializing.set(false)
                }
            }
            // Start async token collector if not already started
            if (tokenCollectorStarted.compareAndSet(false, true)) {
                tokenScope.launch {
                    try {
                        store.tokenFlow.collect { token ->
                            tokenCache.set(token)
                        }
                    } catch (e: Exception) {
                        // Re-throw CancellationException to respect coroutine cancellation
                        if (e is CancellationException) throw e
                        Clogger.e("NetworkModule", "Token flow collection failed", e)
                        tokenCollectorStarted.set(false)
                    }
                }
            }
        }
        
        return Interceptor { chain ->
            ensureTokenCacheInitialized()
            val originalRequest = chain.request()
            // Use cached token value (non-blocking)
            val token = tokenCache.get()
        val requestWithAuth = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val path = originalRequest.url.encodedPath
        val isAuthEndpoint = path.endsWith("/api/users/login") || path.endsWith("/api/users/register")
        val isSyncEndpoint = path.startsWith("/sync/checkins")

        var response = try {
            chain.proceed(requestWithAuth)
        } catch (e: Exception) {
            // Network errors (timeouts, connection issues) should not trigger logout
            // Only re-throw the exception - don't attempt session restoration on network failures
            throw e
        }

        // Sync endpoints currently return 401/403 from the hosted API - keep session alive and rely on
        // offline-first behavior until the push succeeds instead of nuking the token.
        // TODO(#sync-auth-guard): Remove once Summit sync endpoints return 2xx (or correct auth errors) again.
        if (isSyncEndpoint && (response.code == 401 || response.code == 403)) {
            Clogger.w("NetworkModule", "Sync endpoint auth error (${response.code}) - keeping session intact for offline queue")
            return@Interceptor response
        }

        // Handle auth errors (401/403) only - 500 errors are server errors, not auth errors
        if (!isAuthEndpoint && (response.code == 401 || response.code == 403)) {
            // Attempt session restoration for auth errors only
            val originalToken = token
            when (val restoreResult = sessionManager.tryRestoreSession()) {
                SessionManager.SessionRestoreResult.RESTORED -> {
                    // Session restored successfully - retry the original request with new token
                    response.close()
                    // Use cached token (should be updated by now via flow)
                    // Small delay to ensure cache is updated after tokenStore.set() in tryRestoreSession
                    var refreshedToken = tokenCache.get()
                    if (refreshedToken == originalToken) {
                        // Cache might not be updated yet, wait briefly
                        try {
                            refreshedToken = runBlocking {
                                withTimeout(400) {
                                    var current = tokenCache.get()
                                    while (current == originalToken && current != null) {
                                        delay(50)
                                        current = tokenCache.get()
                                    }
                                    current
                                }
                            }
                        } catch (e: Exception) {
                            // Timeout - use cache value anyway
                            refreshedToken = tokenCache.get()
                        }
                    }
                    val retryRequest = originalRequest.newBuilder().apply {
                        if (!refreshedToken.isNullOrBlank()) {
                            header("Authorization", "Bearer $refreshedToken")
                        } else {
                            removeHeader("Authorization")
                        }
                    }.build()
                    response = chain.proceed(retryRequest)
                    // If retry still fails with 401/403 after restoration, credentials are invalid
                    if (response.code == 401 || response.code == 403) {
                        Clogger.w("NetworkModule", "Retry after session restoration still failed with ${response.code}. Fully logging out to redirect to login")
                        tokenCache.set(null) // Clear cache
                        // Clear token store asynchronously (don't block)
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            store.clear()
                        }
                        // Force full logout to ensure user goes to login screen, not locked screen
                        sessionManager.forceLogout(forceFullLogout = true)
                    }
                    // If retry still fails with 500, it's a real server error, not auth-related
                }
                SessionManager.SessionRestoreResult.ALREADY_RESTORING -> {
                    // Another request is already restoring the session - wait for it to complete
                    val restorationSucceeded = sessionManager.waitForRestoration(originalToken)
                    if (restorationSucceeded) {
                        // Restoration completed successfully - retry with new token
                        response.close()
                        // Use cached token (should be updated by now)
                        var refreshedToken = tokenCache.get()
                        if (refreshedToken == originalToken) {
                            // Cache might not be updated yet, wait briefly
                            try {
                                refreshedToken = runBlocking {
                                    withTimeout(400) {
                                        var current = tokenCache.get()
                                        while (current == originalToken && current != null) {
                                            delay(50)
                                            current = tokenCache.get()
                                        }
                                        current
                                    }
                                }
                            } catch (e: Exception) {
                                refreshedToken = tokenCache.get()
                            }
                        }
                        val retryRequest = originalRequest.newBuilder().apply {
                            if (!refreshedToken.isNullOrBlank()) {
                                header("Authorization", "Bearer $refreshedToken")
                            } else {
                                removeHeader("Authorization")
                            }
                        }.build()
                        response = chain.proceed(retryRequest)
                        // If retry still fails with 401/403 after restoration, credentials are invalid
                        if (response.code == 401 || response.code == 403) {
                            Clogger.w("NetworkModule", "Retry after session restoration (waited) still failed with ${response.code}. Fully logging out to redirect to login")
                            tokenCache.set(null) // Clear cache
                            // Clear token store asynchronously (don't block)
                            CoroutineScope(Dispatchers.IO).launch {
                                store.clear()
                            }
                            // Force full logout to ensure user goes to login screen, not locked screen
                            sessionManager.forceLogout(forceFullLogout = true)
                        }
                    }
                    // If restoration failed or timed out, return the original error response
                    // (don't close it - let the caller handle it)
                }
                SessionManager.SessionRestoreResult.INVALID_CREDENTIALS -> {
                    // INVALID_CREDENTIALS means stored credentials are wrong (line 91 guarantees 401/403)
                    // Fully logout (including Firebase) to redirect user to login screen
                    Clogger.w("NetworkModule", "Session restore failed: invalid credentials. Fully logging out to redirect to login")
                    // Clear the invalid token from cache immediately
                    tokenCache.set(null)
                    // Clear token store asynchronously (don't block interceptor)
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        store.clear()
                    }
                    // Force full logout (including Firebase) to redirect to login screen
                    // This ensures user goes to Unauthenticated state, not Locked state
                    sessionManager.forceLogout(forceFullLogout = true)
                    // Return the original error response - caller should handle it
                }
                SessionManager.SessionRestoreResult.NO_CREDENTIALS -> {
                    // No credentials stored - this means credentials were already cleared
                    // or user never completed login. Don't force logout as there's nothing to clear.
                    // Just return the error response - user needs to login again.
                    Clogger.w("NetworkModule", "Session restore failed: no credentials stored")
                }
                SessionManager.SessionRestoreResult.NETWORK_ERROR,
                SessionManager.SessionRestoreResult.SERVER_ERROR -> {
                    // For 500 errors, if session restoration failed, it might be a real server error
                    // Don't logout - just return the error response
                    // For 401/403, keep the user logged in and allow retries
                    // Preserve credentials even if session restore fails due to server/network issues
                    Clogger.w("NetworkModule", "Session restore failed: ${restoreResult}. Preserving credentials due to network/server error")
                }
            }
        }

        response
        }
    }

    @Provides @Singleton
    fun okHttp(auth: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(auth)
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(20))
            .writeTimeout(java.time.Duration.ofSeconds(20))
        
        // Only add logging interceptor in debug builds
        createLoggingInterceptor()?.let { logger ->
            builder.addInterceptor(logger)
        }
        
        return builder.build()
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
        val builder = OkHttpClient.Builder()
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(20))
            .writeTimeout(java.time.Duration.ofSeconds(20))
        
        // Only add logging interceptor in debug builds
        createLoggingInterceptor()?.let { logger ->
            builder.addInterceptor(logger)
        }
        
        val client = builder.build()
        
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