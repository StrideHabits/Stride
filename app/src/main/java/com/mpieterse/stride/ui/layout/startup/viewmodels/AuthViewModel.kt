package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.GlobalAuthenticationListener
import com.mpieterse.stride.core.services.GoogleAuthenticationClient
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.repo.AuthRepository
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
    private val authApi: AuthRepository,
    private val globalAuthListener: GlobalAuthenticationListener
) : ViewModel() {

    val signInForm = SignInFormViewModel()
    val signUpForm = SignUpFormViewModel()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    
    private var isAuthenticating = false

    init {
        if (authService.isUserSignedIn()) {
            _authState.value = AuthState.Locked
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUp() {
        viewModelScope.launch {
            // Prevent multiple simultaneous authentication attempts
            if (isAuthenticating) {
                Clogger.d("AuthViewModel", "Sign-up already in progress, ignoring duplicate request")
                return@launch
            }

            // Validate form first
            signUpForm.validateForm()
            if (!(signUpForm.isFormValid.value)) {
                return@launch
            }

            // Check password match
            val password = signUpForm.formState.value.passwordDefault.value
            val passwordConfirm = signUpForm.formState.value.passwordConfirm.value
            if (password != passwordConfirm) {
                _authState.value = AuthState.Error("Passwords do not match")
                return@launch
            }

            isAuthenticating = true
            globalAuthListener.setAuthInProgress(true)
            _authState.value = AuthState.Loading

            _authState.value = runCatching {
                val email = signUpForm.formState.value.identity.value.trim()
                
                Clogger.d("AuthViewModel", "Starting sign-up for email: $email")
                
                // Step 1: Create Firebase account
                authService.signUpAsync(
                    email = email,
                    password = password
                )

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }
                
                Clogger.d("AuthViewModel", "Firebase registration successful for: ${user.email}")

                // Step 2: Register with API using actual password (not UID)
                Clogger.d("AuthViewModel", "Attempting API registration for: ${user.email}")
                val apiRegistrationState = authApi.register(
                    name = user.email!!,
                    email = user.email!!,
                    pass = password
                )

                // Step 3: Login to API to get JWT token
                val apiLoginState = when (apiRegistrationState) {
                    is ApiResult.Ok -> {
                        Clogger.d("AuthViewModel", "API registration successful, attempting login")
                        authApi.login(user.email!!, pass = password)
                    }
                    is ApiResult.Err -> {
                        // If registration fails (e.g., user already exists), try to login
                        if (apiRegistrationState.code == 409 || apiRegistrationState.code == 400) {
                            Clogger.d("AuthViewModel", "User already exists in API (code: ${apiRegistrationState.code}), attempting login")
                            authApi.login(user.email!!, pass = password)
                        } else {
                            Clogger.e("AuthViewModel", "Registration failed: ${apiRegistrationState.message} (code: ${apiRegistrationState.code})")
                            throw Exception("Registration failed: ${apiRegistrationState.message}")
                        }
                    }
                }

                if (apiLoginState is ApiResult.Err) {
                    // Only logout Firebase on actual authentication errors (401, 403)
                    // Network errors (timeout, connection issues) should not logout the user
                    val isAuthError = apiLoginState.code != null && (apiLoginState.code == 401 || apiLoginState.code == 403)
                    Clogger.w("AuthViewModel", "API login failed: ${apiLoginState.message} (code: ${apiLoginState.code}, isAuthError: $isAuthError)")
                    if (isAuthError) {
                        Clogger.d("AuthViewModel", "Logging out Firebase due to authentication error")
                        // Keep isAuthInProgress = true during logout to prevent listener from interfering
                        // The flag will be cleared in the .also block after error handling
                        authService.logout()
                    }
                    throw Exception("Login failed: ${apiLoginState.message}")
                }

                Clogger.d("AuthViewModel", "Sign-up successful, setting state to Locked")
                AuthState.Locked
            }.getOrElse { exception ->
                // Map errors to user-friendly messages
                val errorMessage = when {
                    exception.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timed out. Please check your connection and try again."
                    exception.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection and try again."
                    exception.message?.contains("409", ignoreCase = true) == true -> 
                        "An account with this email already exists. Please sign in instead."
                    exception.message?.contains("401", ignoreCase = true) == true || 
                    exception.message?.contains("403", ignoreCase = true) == true -> 
                        "Authentication failed. Please try again."
                    else -> exception.message ?: "Sign-up failed. Please try again."
                }
                Clogger.e("AuthViewModel", "Sign-up error: $errorMessage", exception)
                AuthState.Error(errorMessage)
            }.also {
                isAuthenticating = false
                globalAuthListener.setAuthInProgress(false)
            }
        }
    }

    fun signIn() { //This method handles user authentication using Firebase Authentication and API integration (Android Developers, 2024).
        viewModelScope.launch {
            // Prevent multiple simultaneous authentication attempts
            if (isAuthenticating) {
                Clogger.d("AuthViewModel", "Sign-in already in progress, ignoring duplicate request")
                return@launch
            }

            // Validate form first
            signInForm.validateForm()
            if (!(signInForm.isFormValid.value)) {
                return@launch
            }

            isAuthenticating = true
            globalAuthListener.setAuthInProgress(true)
            _authState.value = AuthState.Loading

            _authState.value = runCatching {
                val email = signInForm.formState.value.identity.value.trim()
                val password = signInForm.formState.value.password.value
                
                Clogger.d("AuthViewModel", "Starting sign-in for email: $email")
                
                // Step 1: Authenticate with Firebase
                authService.signInAsync(
                    email = email,
                    password = password
                )

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }
                
                Clogger.d("AuthViewModel", "Firebase authentication successful for: ${user.email}")

                // Step 2: Login to API using actual password (not UID) to get JWT token
                Clogger.d("AuthViewModel", "Attempting API login for: ${user.email}")
                var apiLoginState = authApi.login(
                    email = user.email!!,
                    pass = password
                )

                // If login fails with 401, the user might exist in Firebase but not in the API
                // Try to register them first, then login again
                if (apiLoginState is ApiResult.Err && apiLoginState.code == 401) {
                    Clogger.d("AuthViewModel", "API login returned 401, attempting to register user in API")
                    val apiRegistrationState = authApi.register(
                        name = user.email!!,
                        email = user.email!!,
                        pass = password
                    )
                    
                    when (apiRegistrationState) {
                        is ApiResult.Ok -> {
                            Clogger.d("AuthViewModel", "User registered in API, attempting login again")
                            // User was successfully registered, try login again
                            apiLoginState = authApi.login(user.email!!, pass = password)
                        }
                        is ApiResult.Err -> {
                            // Registration failed - check the error code
                            when (apiRegistrationState.code) {
                                409 -> {
                                    Clogger.w("AuthViewModel", "User already exists in API but login failed - password mismatch")
                                    // User exists but password doesn't match - this is a real auth error
                                    // Don't throw yet, let the login error handling below take care of it
                                }
                                else -> {
                                    Clogger.e("AuthViewModel", "Registration failed: ${apiRegistrationState.message} (code: ${apiRegistrationState.code})")
                                    // For other errors, try login one more time in case registration partially succeeded
                                    apiLoginState = authApi.login(user.email!!, pass = password)
                                }
                            }
                        }
                    }
                }

                if (apiLoginState is ApiResult.Err) {
                    // Only logout Firebase on actual authentication errors (401, 403)
                    // Network errors (timeout, connection issues) should not logout the user
                    val isAuthError = apiLoginState.code != null && (apiLoginState.code == 401 || apiLoginState.code == 403)
                    Clogger.w("AuthViewModel", "API login failed: ${apiLoginState.message} (code: ${apiLoginState.code}, isAuthError: $isAuthError)")
                    if (isAuthError) {
                        Clogger.d("AuthViewModel", "Logging out Firebase due to authentication error")
                        // Keep isAuthInProgress = true during logout to prevent listener from interfering
                        // The flag will be cleared in the .also block after error handling
                        authService.logout()
                    }
                    throw Exception("API login failed: ${apiLoginState.message}")
                }

                Clogger.d("AuthViewModel", "Sign-in successful, setting state to Locked")
                AuthState.Locked
            }.getOrElse { exception ->
                // Map errors to user-friendly messages
                val errorMessage = when {
                    exception.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timed out. Please check your connection and try again."
                    exception.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection and try again."
                    exception.message?.contains("password", ignoreCase = true) == true -> 
                        "Invalid email or password. Please try again."
                    exception.message?.contains("401", ignoreCase = true) == true || 
                    exception.message?.contains("403", ignoreCase = true) == true -> 
                        "Invalid email or password. Please try again."
                    exception.message?.contains("user", ignoreCase = true) == true -> 
                        "No account found with this email. Please sign up first."
                    else -> exception.message ?: "Sign-in failed. Please try again."
                }
                Clogger.e("AuthViewModel", "Sign-in error: $errorMessage", exception)
                AuthState.Error(errorMessage)
            }.also {
                isAuthenticating = false
                globalAuthListener.setAuthInProgress(false)
            }
        }
    }

    fun googleSignIn() { //This method handles Google Sign-In authentication using Firebase Authentication (Android Developers, 2024).
        viewModelScope.launch {
            // Prevent multiple simultaneous authentication attempts
            if (isAuthenticating) {
                Clogger.d("AuthViewModel", "Google sign-in already in progress, ignoring duplicate request")
                return@launch
            }

            isAuthenticating = true
            globalAuthListener.setAuthInProgress(true)
            _authState.value = AuthState.Loading

            _authState.value = runCatching {
                ssoClient.executeAuthenticationTransactionAsync()

                val user = authService.getCurrentUser() ?: error("No Firebase user found")
                requireNotNull(user.email) {
                    "User email is null"
                }

                // For Google Sign-In, we use Firebase UID as the password since we don't have a password
                // This is acceptable because Google OAuth already authenticated the user
                val firebaseUid = user.uid

                // Try to register with API (will fail if user already exists, which is fine)
                val apiRegistrationState = authApi.register(
                    name = user.email!!,
                    email = user.email!!,
                    pass = firebaseUid
                )

                // Always try to login after registration attempt
                val apiLoginState = when (apiRegistrationState) {
                    is ApiResult.Ok -> authApi.login(user.email!!, pass = firebaseUid)
                    is ApiResult.Err -> {
                        // If registration fails (user exists), try to login
                        if (apiRegistrationState.code == 409 || apiRegistrationState.code == 400) {
                            authApi.login(user.email!!, pass = firebaseUid)
                        } else {
                            throw Exception("Registration failed: ${apiRegistrationState.message}")
                        }
                    }
                }

                if (apiLoginState is ApiResult.Err) {
                    // Only logout Firebase on actual authentication errors (401, 403)
                    // Network errors (timeout, connection issues) should not logout the user
                    val isAuthError = apiLoginState.code != null && (apiLoginState.code == 401 || apiLoginState.code == 403)
                    if (isAuthError) {
                        Clogger.d("AuthViewModel", "Logging out Firebase due to authentication error")
                        // Keep isAuthInProgress = true during logout to prevent listener from interfering
                        // The flag will be cleared in the .also block after error handling
                        authService.logout()
                    }
                    throw Exception("API login failed: ${apiLoginState.message}")
                }

                AuthState.Locked
            }.getOrElse { exception ->
                // Map errors to user-friendly messages
                val errorMessage = when {
                    exception.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timed out. Please check your connection and try again."
                    exception.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection and try again."
                    exception.message?.contains("401", ignoreCase = true) == true || 
                    exception.message?.contains("403", ignoreCase = true) == true -> 
                        "Authentication failed. Please try again."
                    else -> exception.message ?: "Google sign-in failed. Please try again."
                }
                AuthState.Error(errorMessage)
            }.also {
                isAuthenticating = false
                globalAuthListener.setAuthInProgress(false)
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

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}