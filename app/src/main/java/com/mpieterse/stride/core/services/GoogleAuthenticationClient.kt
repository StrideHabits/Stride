package com.mpieterse.stride.core.services

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mpieterse.stride.BuildConfig
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAuthenticationClient
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val server: FirebaseAuth,
    private val authService: AuthenticationService,
    private val credentialManager: CredentialManager
) {
    companion object {
        private const val TAG = "GoogleAuthenticationClient"
    }


// --- Functions


    suspend fun executeAuthenticationTransactionAsync() { //This method handles Google Sign-In authentication using Firebase Authentication and Credential Manager (Google Inc., 2024).
        if (authService.isUserSignedIn()) {
            Clogger.i(
                TAG, "User is already signed-in."
            )

            return
        }

        val credential = getCredentials().credential
        check((credential is CustomCredential) && (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            "Invalid credential type."
        }

        try {
            val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(
                tokenCredential.idToken, null
            )

            val result = server.signInWithCredential(authCredential).await()
            when (result.user) {
                null -> {
                    Clogger.d(
                        TAG, "Failed to sign-up a user using their credentials."
                    )

                    throw IllegalStateException("User is null after sign-up.")
                }

                else -> {
                    Clogger.d(
                        TAG, "Successfully completed sign-up transaction."
                    )
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Clogger.e(
                TAG, e.message.toString(), e
            )

            throw e
        }
    }


// --- Internals


    private suspend fun getCredentials(): GetCredentialResponse { //This method retrieves Google credentials using Android Credential Manager (Android Developers, 2024).
        val request = GetCredentialRequest.Builder().apply {
            addCredentialOption(
                GetGoogleIdOption.Builder().apply {
                    setAutoSelectEnabled(false)
                    setFilterByAuthorizedAccounts(false)
                    setServerClientId(
                        BuildConfig.GOOGLE_SERVER_CLIENT_ID
                    )
                }.build()
            )
        }.build()

        // Get response from credential manager
        return credentialManager.getCredential(
            request = request, context = context
        )
    }
}