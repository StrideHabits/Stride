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
    }
}