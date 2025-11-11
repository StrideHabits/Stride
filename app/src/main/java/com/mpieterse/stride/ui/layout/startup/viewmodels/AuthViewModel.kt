package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.GoogleAuthenticationClient
import com.mpieterse.stride.data.repo.concrete.AuthRepository
import com.mpieterse.stride.ui.layout.startup.models.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val authService: AuthenticationService,
    private val ssoClient: GoogleAuthenticationClient,
    private val authApi: AuthRepository
) : ViewModel() {

    val signInForm = SignInFormViewModel()
    val signUpForm = SignUpFormViewModel()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    init {
        if (authService.isUserSignedIn()) {
            _authState.value = AuthState.Locked
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUp() {
        viewModelScope.launch {
            signUpForm.validateForm()
            if (!(signUpForm.isFormValid.value)) {
                return@launch
            }

            _authState.value = runCatching {
                authService.signUpAsync(
                    email = signUpForm.formState.value.identity.value,
                    password = signUpForm.formState.value.passwordDefault.value
                )

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }

                val apiRegistrationState = authApi.register(
                    user.email!!, user.email!!, user.uid
                )

                val apiLoginState = when (apiRegistrationState) {
                    is ApiResult.Ok -> authApi.login(user.email!!, user.uid)
                    is ApiResult.Err -> authApi.login(user.email!!, user.uid)
                }

                if (apiLoginState is ApiResult.Err) {
                    authService.logout()
                    throw Exception(apiLoginState.message)
                }

                AuthState.Locked
            }.getOrElse {
                AuthState.Error(
                    it.message ?: "Sign-up failed"
                )
            }
        }
    }

    fun signIn() { //This method handles user authentication using Firebase Authentication and API integration (Android Developers, 2024).
        viewModelScope.launch {
            signInForm.validateForm()
            if (!(signInForm.isFormValid.value)) {
                return@launch
            }

            _authState.value = runCatching {
                authService.signInAsync(
                    email = signInForm.formState.value.identity.value,
                    password = signInForm.formState.value.password.value
                )

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }

                val apiLoginState = authApi.login(
                    user.email!!, user.uid
                )

                if (apiLoginState is ApiResult.Err) {
                    authService.logout()
                    throw Exception(apiLoginState.message)
                }

                AuthState.Locked
            }.getOrElse {
                AuthState.Error(
                    it.message ?: "Sign-in failed"
                )
            }
        }
    }

    fun googleSignIn() { //This method handles Google Sign-In authentication using Firebase Authentication (Android Developers, 2024).
        viewModelScope.launch {
            _authState.value = runCatching {
                ssoClient.executeAuthenticationTransactionAsync()

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }

                val apiRegistrationState = authApi.register(
                    user.email!!, user.email!!, user.uid
                )

                val apiLoginState = when (apiRegistrationState) {
                    is ApiResult.Ok -> authApi.login(user.email!!, user.uid)
                    is ApiResult.Err -> authApi.login(user.email!!, user.uid)
                }

                if (apiLoginState is ApiResult.Err) {
                    authService.logout()
                    throw Exception(apiLoginState.message)
                }

                AuthState.Locked
            }.getOrElse {
                AuthState.Error(
                    it.message ?: "Google sign-in failed"
                )
            }
        }
    }

    fun unlockWithBiometrics(success: Boolean) { //This method handles biometric authentication unlock using Android biometric APIs (Android Developers, 2024).
        if (success) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun logout() { //This method handles user logout using Firebase Authentication (Android Developers, 2024).
        authService.logout()
        _authState.value = AuthState.Unauthenticated
    }
}