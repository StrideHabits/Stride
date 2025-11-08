package com.mpieterse.stride.core.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.ui.layout.startup.roots.AuthActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Global monitor for Firebase's authentication state.
 *
 * @property listen
 * @property ignore
 */
class GlobalAuthenticationListener
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseAuthService: AuthenticationService,
    private val configurationService: ConfigurationService
) : FirebaseAuth.AuthStateListener, DefaultLifecycleObserver {
    companion object {
        private const val TAG = "GlobalAuthenticationListener"
    }


// --- Fields


    private val appScope = CoroutineScope(Dispatchers.Main)
    private var isAuthInProgress = false


// --- Contracts


    /**
     * Listen for Firebase authentication state changes.
     */
    fun listen() {
        Clogger.d(
            TAG, "Listening for Firebase authentication state changes"
        )

        firebaseAuth.addAuthStateListener(this)
    }


    /**
     * Ignore all Firebase authentication state changes.
     */
    fun ignore() {
        Clogger.d(
            TAG, "Ignoring all Firebase authentication state changes"
        )

        firebaseAuth.removeAuthStateListener(this)
    }


// --- Internals


    /**
     * Checks the validity of the current authentication state.
     */
    private fun checkAuthValidity() = appScope.launch {
        try {
            if (!firebaseAuthService.isUserSignedIn()) {
                secureApplication()
                return@launch
            }
        } catch (e: Exception) {
            secureApplication()
            Clogger.e(
                TAG, "Failed to check authentication validity", e
            )
        }
    }


    /**
     * Secures the application when authentication is revoked.
     */
    private fun secureApplication() {
        // Don't interrupt if authentication is actively in progress
        // This prevents the listener from interfering during login/signup flows
        if (isAuthInProgress) {
            Clogger.d(
                TAG, "Skipping secureApplication: authentication in progress"
            )
            return
        }

        Clogger.d(
            TAG, "Securing the application due to revoked authentication"
        )

        val navigationIntent = Intent(context, AuthActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        context.startActivity(navigationIntent)
        appScope.launch {
            configurationService.erase()
        }
    }

    /**
     * Marks that authentication is in progress. This prevents the listener from
     * interfering with the authentication flow.
     */
    fun setAuthInProgress(inProgress: Boolean) {
        isAuthInProgress = inProgress
        Clogger.d(TAG, "Authentication in progress: $inProgress")
    }

    /**
     * Handler for Firebase authentication state changes.
     */
    override fun onAuthStateChanged(server: FirebaseAuth) {
        // Don't trigger secureApplication if authentication is actively in progress
        // This prevents interrupting the auth flow when Firebase state changes during login/signup
        if (server.currentUser == null && !isAuthInProgress) {
            secureApplication()
        } else if (server.currentUser == null && isAuthInProgress) {
            Clogger.d(TAG, "Auth state changed to null during authentication - ignoring")
        }
    }


// --- Lifecycle


    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        checkAuthValidity()
    }
}