package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.GlobalAuthenticationListener
import com.mpieterse.stride.core.services.GoogleAuthenticationClient
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.repo.concrete.AuthRepository
import com.mpieterse.stride.data.store.CredentialsStore
import com.mpieterse.stride.ui.layout.startup.models.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val authService: AuthenticationService,
    private val ssoClient: GoogleAuthenticationClient,
    private val authApi: AuthRepository,
    private val globalAuthListener: GlobalAuthenticationListener,
    private val credentialsStore: CredentialsStore
) : ViewModel() {

    val signInForm = SignInFormViewModel()
    val signUpForm = SignUpFormViewModel()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        if (authService.isUserSignedIn()) {
            _authState.value = AuthState.Locked
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }

    fun signUp() {
        viewModelScope.launch {
            clearError()
            signUpForm.validateForm()
            if (!(signUpForm.isFormValid.value)) {
                return@launch
            }

            _isLoading.value = true
            try {
                val rawPassword = signUpForm.formState.value.passwordDefault.value
                val trimmedPassword = rawPassword.trim()

                authService.signUpAsync(
                    email = signUpForm.formState.value.identity.value,
                    password = rawPassword
                )

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }

                // Use trimmed password for registration to match login behavior
                val apiRegistrationState = authApi.register(
                    user.email!!,
                    user.email!!,
                    trimmedPassword
                )

                // Use trimmed password for login to match registration
                val apiLoginState = when (apiRegistrationState) {
                    is ApiResult.Ok -> authApi.login(user.email!!, trimmedPassword)
                    is ApiResult.Err -> {
                        val isNetworkError = apiRegistrationState.code == null ||
                            apiRegistrationState.message.contains("timeout", ignoreCase = true) ||
                            apiRegistrationState.message.contains("network", ignoreCase = true) ||
                            apiRegistrationState.message.contains("connection", ignoreCase = true)

                        if (isNetworkError) {
                            apiRegistrationState
                        } else {
                            authApi.login(user.email!!, trimmedPassword)
                        }
                    }
                }

                if (apiLoginState is ApiResult.Err) {
                    // Check if it's an authentication error (401, 403) or a network error
                    val isAuthError = apiLoginState.code != null && (apiLoginState.code == 401 || apiLoginState.code == 403)
                    val isNetworkError = apiLoginState.code == null || apiLoginState.message.contains("timeout", ignoreCase = true) || 
                                         apiLoginState.message.contains("network", ignoreCase = true) ||
                                         apiLoginState.message.contains("connection", ignoreCase = true)
                    
                    // Set error message first
                    val errorMsg = when {
                        isAuthError -> apiLoginState.message ?: "Authentication failed. Please check your credentials and try again."
                        isNetworkError -> "Network error. Please check your internet connection and try again."
                        else -> apiLoginState.message ?: "Failed to authenticate with server. Please try again."
                    }
                    
                    // Set flag BEFORE logging out to prevent navigation loop
                    globalAuthListener.setHandlingError(true)
                    
                    try {
                        authService.logout()
                    } catch (e: Exception) {
                        // Ignore logout errors
                    }
                    
                    // Set UI state
                    _errorMessage.value = errorMsg
                    _authState.value = AuthState.Unauthenticated
                    
                    // Reset flag after a longer delay to ensure auth state listener has processed
                    delay(500)
                    globalAuthListener.setHandlingError(false)
                    
                    return@launch
                }

                _authState.value = AuthState.Locked
                credentialsStore.save(user.email!!, trimmedPassword)
            } catch (e: Exception) {
                // On Firebase auth error, show error message
                // Don't logout to avoid navigation issues - user can retry
                _errorMessage.value = e.message ?: "Sign-up failed. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn() { //This method handles user authentication using Firebase Authentication and API integration (Android Developers, 2024).
        viewModelScope.launch {
            clearError()
            signInForm.validateForm()
            if (!(signInForm.isFormValid.value)) {
                return@launch
            }

            _isLoading.value = true
            try {
                val rawPassword = signInForm.formState.value.password.value
                val trimmedPassword = rawPassword.trim()

                authService.signInAsync(
                    email = signInForm.formState.value.identity.value,
                    password = rawPassword
                )

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }

                val invalidCredentialMessage = "Invalid credentials. Please check your email and password."
                // Use trimmed password first to match registration behavior (registration always uses trimmed)
                var apiLoginState = authApi.login(
                    user.email!!,
                    trimmedPassword
                )

                if (apiLoginState is ApiResult.Err) {
                    val message = apiLoginState.message.lowercase()
                    // If trimmed password fails and password has whitespace, try raw password
                    // (for legacy users who might have registered with whitespace)
                    val shouldRetryWithRaw = (message.contains("invalid credentials") || message.contains("password")) &&
                        trimmedPassword != rawPassword
                    if (shouldRetryWithRaw) {
                        Clogger.d("AuthViewModel", "Retrying login with raw password (trimmed failed, trying raw for legacy users)")
                        apiLoginState = authApi.login(user.email!!, rawPassword)
                    }
                    // Handle 404 - user might not exist in Summit API, try to register
                    if (apiLoginState is ApiResult.Err && apiLoginState.code == 404) {
                        Clogger.d("AuthViewModel", "User not found in Summit API (404), attempting registration")
                        // Use trimmed password for registration to match login
                        val reRegister = authApi.register(user.email!!, user.email!!, trimmedPassword)
                        if (reRegister is ApiResult.Ok) {
                            // Registration successful, try login again with trimmed password (same as registration)
                            apiLoginState = authApi.login(user.email!!, trimmedPassword)
                            if (apiLoginState is ApiResult.Err) {
                                Clogger.e("AuthViewModel", "Login failed after registration: ${apiLoginState.code} ${apiLoginState.message}")
                            }
                        } else if (reRegister is ApiResult.Err) {
                            // Registration failed - create a new error with the same info for login
                            Clogger.e("AuthViewModel", "Registration failed after 404: ${reRegister.code} ${reRegister.message}")
                            apiLoginState = ApiResult.Err(reRegister.code, reRegister.message)
                        }
                    }
                }

                if (apiLoginState is ApiResult.Err) {
                    // Check if it's an authentication error (401, 403) or a network error
                    val isAuthError = apiLoginState.code != null && (apiLoginState.code == 401 || apiLoginState.code == 403)
                    val isNetworkError = apiLoginState.code == null || apiLoginState.message.contains("timeout", ignoreCase = true) || 
                                         apiLoginState.message.contains("network", ignoreCase = true) ||
                                         apiLoginState.message.contains("connection", ignoreCase = true)
                    val is404Error = apiLoginState.code == 404
                    
                    // Set error message first (before logout to ensure it's visible)
                    val errorMsg = when {
                        isAuthError -> invalidCredentialMessage
                        is404Error -> "Account not found. Please sign up first or check your email address."
                        isNetworkError -> "Network error. Please check your internet connection and try again."
                        else -> apiLoginState.message ?: "Failed to authenticate with server. Please try again."
                    }
                    
                    Clogger.e("AuthViewModel", "Login failed: ${apiLoginState.code} - $errorMsg")
                    
                    // Set flag FIRST to prevent navigation before we do anything else
                    globalAuthListener.setHandlingError(true)
                    
                    // Set UI state so error message is visible
                    _errorMessage.value = errorMsg
                    _authState.value = AuthState.Unauthenticated
                    
                    // DON'T logout - keep Firebase auth active so user can retry without re-entering password
                    // The error message will be visible and user can try again
                    // Only logout would be needed if we want to clear Firebase session, but that causes navigation
                    
                    // Keep flag set to prevent any navigation
                    // Reset after delay to allow future navigation if needed
                    delay(1000)
                    globalAuthListener.setHandlingError(false)
                    
                    return@launch
                }

                _authState.value = AuthState.Locked
                credentialsStore.save(user.email!!, trimmedPassword)
            } catch (e: Exception) {
                // On Firebase auth error, show error message
                // Don't logout to avoid navigation issues - user can retry
                _errorMessage.value = e.message ?: "Sign-in failed. Please check your credentials and try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun googleSignIn() { //This method handles Google Sign-In authentication using Firebase Authentication (Android Developers, 2024).
        viewModelScope.launch {
            clearError()
            _isLoading.value = true
            try {
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
                    is ApiResult.Err -> {
                        // Try to login even if registration fails (user might already exist)
                        authApi.login(user.email!!, user.uid)
                    }
                }

                if (apiLoginState is ApiResult.Err) {
                    // Check if it's an authentication error (401, 403) or a network error
                    val isAuthError = apiLoginState.code != null && (apiLoginState.code == 401 || apiLoginState.code == 403)
                    val isNetworkError = apiLoginState.code == null || apiLoginState.message.contains("timeout", ignoreCase = true) || 
                                         apiLoginState.message.contains("network", ignoreCase = true) ||
                                         apiLoginState.message.contains("connection", ignoreCase = true)
                    
                    // Set error message first
                    val errorMsg = when {
                        isAuthError -> apiLoginState.message ?: "Authentication failed. Please try again."
                        isNetworkError -> "Network error. Please check your internet connection and try again."
                        else -> apiLoginState.message ?: "Failed to authenticate with server. Please try again."
                    }
                    
                    // Set flag BEFORE logging out to prevent navigation loop
                    globalAuthListener.setHandlingError(true)
                    
                    try {
                        authService.logout()
                    } catch (e: Exception) {
                        // Ignore logout errors
                    }
                    
                    // Set UI state
                    _errorMessage.value = errorMsg
                    _authState.value = AuthState.Unauthenticated
                    
                    // Reset flag after a longer delay to ensure auth state listener has processed
                    delay(500)
                    globalAuthListener.setHandlingError(false)
                    
                    return@launch
                }

                _authState.value = AuthState.Locked
                credentialsStore.save(user.email!!, user.uid)
            } catch (e: Exception) {
                // On Firebase auth error, show error message
                // Don't logout to avoid navigation issues - user can retry
                _errorMessage.value = e.message ?: "Google sign-in failed. Please try again."
            } finally {
                _isLoading.value = false
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
        credentialsStore.clear()
        _authState.value = AuthState.Unauthenticated
    }
}