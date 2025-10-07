package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val isAuthenticated: Boolean = false
    )
    
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state
    
    fun login(email: String, password: String, onSuccess: () -> Unit) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        
        when (val result = authRepository.login(email, password)) {
            is ApiResult.Ok -> {
                _state.value = _state.value.copy(
                    loading = false,
                    isAuthenticated = true,
                    error = null
                )
                onSuccess()
            }
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = result.message ?: "Login failed"
                )
            }
        }
    }
    
    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        
        when (val result = authRepository.register(name, email, password)) {
            is ApiResult.Ok -> {
                // After successful registration, automatically log in
                login(email, password, onSuccess)
            }
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = result.message ?: "Registration failed"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.GoogleAuthenticationClient
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class AuthViewModel 
@Inject constructor(
    private val authService: AuthenticationService
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }


    @Inject
    lateinit var ssoClient: GoogleAuthenticationClient
    
    
// --- Functions
    
    
    fun isUserSignedIn(): Boolean {
        return authService.isUserSignedIn()
    }


    fun googleSignIn(): Boolean {
        var status = false
        viewModelScope.launch {
            Clogger.i(
                TAG, "Signing-in user with Google SSO"
            )

            runCatching {
                val milliseconds = 30_000L
                withTimeout(milliseconds) {
                    ssoClient.executeAuthenticationTransactionAsync()
                }
            }.apply {
                onSuccess {
                    Clogger.d(
                        TAG, "Attempt to authenticate was a success!"
                    )

                    status = true
                }

                onFailure { exception ->
                    Clogger.d(
                        TAG, "Attempt to authenticate was a failure!"
                    )
                }
            }
        }
        
        return status
    }
}