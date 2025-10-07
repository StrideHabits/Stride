package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.GoogleAuthenticationClient
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.repo.AuthRepository
import com.mpieterse.stride.ui.layout.startup.models.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val authService: AuthenticationService,
    private val ssoClient: GoogleAuthenticationClient,
    private val authApi: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    init {
        if (authService.isUserSignedIn()) {
            _authState.value = AuthState.Locked
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            runCatching {
                authService.signUpAsync(email, password)
            }.onSuccess {
                _authState.value = AuthState.Locked
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Sign-up failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            runCatching {
                authService.signInAsync(email, password)
            }.onSuccess {
                _authState.value = AuthState.Locked
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Sign-in failed")
            }
        }
    }

    fun googleSignIn() {
        viewModelScope.launch {
            runCatching {
                ssoClient.executeAuthenticationTransactionAsync()
                val user = authService.getCurrentUser()
                when (val registerState = authApi.register(user!!.email!!, user.email!!, user.uid)) {
                    is ApiResult.Ok -> {
                        when (val loginState = authApi.login( user.email!!, user.uid)) {
                            is ApiResult.Ok -> {
                                // do nothing
                            }
                            is ApiResult.Err -> {
                                throw Exception()
                            }
                        }
                    }
                    is ApiResult.Err -> {
                        when (val loginState = authApi.login( user.email!!, user.uid)) {
                            is ApiResult.Ok -> {
                                // do nothing
                            }
                            is ApiResult.Err -> {
                                throw Exception()
                            }
                        }
                    }
                }
            }.onSuccess {
                _authState.value = AuthState.Locked
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Google sign-in failed")
            }
        }
    }

    fun unlockWithBiometrics(success: Boolean) {
        if (success) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun logout() {
        authService.logout()
        _authState.value = AuthState.Unauthenticated
    }

//    private suspend fun <T> retryAfterTimeout(
//        execute: suspend () -> T
//    ): T {
//        return runCatching {
//            withTimeout(2000F) {
//                execute()
//            }
//        }.recoverCatching { exception ->
//            if (exception is TimeoutCancellationException) {
//                withTimeout(2000F) {
//                    execute()
//                }
//            } else {
//                throw exception
//            }
//        }.getOrThrow()
//    }
}