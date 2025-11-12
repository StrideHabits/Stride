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
     * Forcefully logout both Summit and Firebase sessions.
     */
    fun forceLogout() {
        if (!loggingOut.compareAndSet(false, true)) return

        scope.launch {
            runCatching { tokenStore.clear() }
                .onFailure { Clogger.e(TAG, "Failed to clear Summit token", it) }

            runCatching { credentialsStore.clear() }
                .onFailure { Clogger.e(TAG, "Failed to clear stored credentials", it) }

            runCatching { authenticationService.logout() }
                .onFailure { Clogger.e(TAG, "Failed to logout Firebase auth", it) }

            loggingOut.set(false)
        }
    }

    companion object {
        private const val TAG = "SessionManager"
    }
}

