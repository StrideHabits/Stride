package com.mpieterse.stride.core.services

import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.dto.auth.AuthResponse
import com.mpieterse.stride.data.dto.auth.LoginRequest
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.remote.SummitApiService
import com.mpieterse.stride.data.store.CredentialsStore
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response

@Singleton
class SessionManager @Inject constructor(
    private val tokenStore: TokenStore,
    private val credentialsStore: CredentialsStore,
    private val authenticationService: AuthenticationService,
    @Named("reauth") private val reauthApiService: SummitApiService
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val restoringSession = AtomicBoolean(false)
    private val loggingOut = AtomicBoolean(false)

    enum class SessionRestoreResult {
        RESTORED,
        INVALID_CREDENTIALS,
        NETWORK_ERROR,
        SERVER_ERROR,
        NO_CREDENTIALS,
        ALREADY_RESTORING
    }

    /**
     * Wait for an in-flight session restoration to complete.
     * Polls the token store until the token changes or a timeout occurs.
     *
     * @param originalToken The token value before restoration started
     * @param timeoutMs Maximum time to wait in milliseconds (default 10 seconds)
     * @return true if a new token was detected, false if timeout
     */
    fun waitForRestoration(originalToken: String?, timeoutMs: Long = 10000): Boolean {
        return runBlocking {
            withTimeoutOrNull(timeoutMs) {
                var currentToken = runBlocking { tokenStore.tokenFlow.first() }
                while (currentToken == originalToken && restoringSession.get()) {
                    delay(100) // Poll every 100ms
                    currentToken = runBlocking { tokenStore.tokenFlow.first() }
                }
                currentToken != originalToken && !currentToken.isNullOrBlank()
            } ?: false
        }
    }

    /**
     * Attempt to restore the Summit API session by logging in with stored credentials.
     *
     * @return [SessionRestoreResult] indicating the outcome of the attempt.
     */
    fun tryRestoreSession(): SessionRestoreResult {
        val credentials = credentialsStore.get() ?: return SessionRestoreResult.NO_CREDENTIALS
        if (!restoringSession.compareAndSet(false, true)) return SessionRestoreResult.ALREADY_RESTORING

        return try {
            val response: Response<AuthResponse> = try {
                runBlocking {
                    reauthApiService.login(
                        LoginRequest(
                            email = credentials.email,
                            password = credentials.password
                        )
                    )
                }
            } catch (e: Exception) {
                val isNetworkError = e is java.net.SocketTimeoutException ||
                    e is java.net.ConnectException ||
                    (e is java.io.IOException && (
                        e.message?.lowercase()?.contains("timeout") == true ||
                        e.message?.lowercase()?.contains("network") == true ||
                        e.message?.lowercase()?.contains("connection") == true
                    ))

                if (isNetworkError) {
                    Clogger.e(TAG, "Network error during session restore", e)
                    return SessionRestoreResult.NETWORK_ERROR
                } else {
                    Clogger.e(TAG, "Failed to call Summit login during session restore", e)
                    return SessionRestoreResult.SERVER_ERROR
                }
            }

            val result = when {
                response.isSuccessful -> {
                    response.body()?.token?.let { token ->
                        runBlocking { tokenStore.set(token) }
                        SessionRestoreResult.RESTORED
                    } ?: run {
                        Clogger.e(TAG, "Summit login succeeded but token was null")
                        SessionRestoreResult.SERVER_ERROR
                    }
                }

                response.code() == 401 || response.code() == 403 -> {
                    Clogger.w(TAG, "Summit login failed during session restore: invalid credentials (${response.code()})")
                    SessionRestoreResult.INVALID_CREDENTIALS
                }

                else -> {
                    Clogger.w(TAG, "Summit login failed during session restore: ${response.code()} ${response.message()}")
                    SessionRestoreResult.SERVER_ERROR
                }
            }

            result.also {
                response.errorBody()?.close()
            }
        } finally {
            restoringSession.set(false)
        }
    }

    /**
     * Attempts to logout from both Summit API and Firebase sessions.
     * 
     * **Behavior:**
     * - Always clears the API token
     * - Only clears credentials and Firebase session if Firebase auth is inactive
     * - Preserves credentials and Firebase session if Firebase auth is still active
     * 
     * This conditional behavior is critical for allowing users to re-authenticate
     * when only the API token expires but Firebase authentication is still valid.
     * Preserving credentials enables automatic session restoration on the next request.
     */
    fun forceLogout() {
        if (!loggingOut.compareAndSet(false, true)) return

        scope.launch {
            try {
                // Check if Firebase auth is still active (with error handling)
                val isFirebaseActive = runCatching { authenticationService.isUserSignedIn() }
                    .onFailure { Clogger.e(TAG, "Failed to check Firebase auth state during logout, defaulting to inactive", it) }
                    .getOrDefault(false)
                
                // Clear the API token first (this should always be cleared on logout)
                runCatching { tokenStore.clear() }
                    .onFailure { Clogger.e(TAG, "Failed to clear Summit token", it) }

                // Only clear credentials and Firebase auth if Firebase auth is not active
                // This prevents losing credentials when only the API token expired
                // but Firebase authentication is still valid
                if (!isFirebaseActive) {
                    Clogger.d(TAG, "Firebase auth inactive, clearing stored credentials and Firebase session")
                    runCatching { credentialsStore.clear() }
                        .onFailure { Clogger.e(TAG, "Failed to clear stored credentials", it) }
                    
                    // Clear Firebase auth session (should already be cleared, but be safe)
                    runCatching { authenticationService.logout() }
                        .onFailure { Clogger.e(TAG, "Failed to logout Firebase auth", it) }
                } else {
                    Clogger.w(TAG, "Firebase auth still active - preserving credentials and Firebase session to allow re-authentication")
                    // Don't clear credentials or Firebase auth if Firebase is still active
                    // The user should be able to re-authenticate with the API using their existing credentials
                    // Only the API token was cleared above, allowing for automatic re-authentication on next request
                }
            } finally {
                // Always reset the flag, even if an exception occurs
                loggingOut.set(false)
            }
        }
    }

    companion object {
        private const val TAG = "SessionManager"
    }
}

