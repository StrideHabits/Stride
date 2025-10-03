package com.mpieterse.stride.core.services

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mpieterse.stride.core.models.results.BiometricError
import com.mpieterse.stride.core.models.results.Final
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BiometricService
@Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isAvailable(): Boolean {
        val result = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }


    fun authenticate(
        activity: FragmentActivity,
        promptInfoBuilder: BiometricPrompt.PromptInfo.Builder,
        onResult: (Final<Unit, BiometricError>) -> Unit
    ) {
        if (!isAvailable()) {
            onResult(Final.Failure(BiometricError.NoSupport))
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(Final.Success(Unit))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val error = when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> BiometricError.Dismissed

                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricError.RateLimit

                        else -> BiometricError.Exception(errorCode, errString.toString())
                    }

                    onResult(Final.Failure(error))
                }

                override fun onAuthenticationFailed() {
                    onResult(Final.Failure(BiometricError.Failed))
                }
            }
        )

        val promptInfo = promptInfoBuilder.build()
        prompt.authenticate(promptInfo)
    }
}