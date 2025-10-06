package com.mpieterse.stride.ui.layout.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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