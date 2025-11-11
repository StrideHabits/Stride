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
    private val configurationService: ConfigurationService,
    private val fcmTokenManager: FcmTokenManager
) : FirebaseAuth.AuthStateListener, DefaultLifecycleObserver {
    companion object {
        private const val TAG = "GlobalAuthenticationListener"
    }


// --- Fields


    private val appScope = CoroutineScope(Dispatchers.Main)
    
    // Flag to temporarily disable navigation when handling errors in AuthActivity
    @Volatile
    private var isHandlingError = false
    
    fun setHandlingError(handling: Boolean) {
        isHandlingError = handling
    }


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
     * If we're already handling an error in AuthActivity, don't navigate to avoid
     * restarting the activity and going back to splash screen.
     */
    private fun secureApplication() {
        Clogger.d(
            TAG, "Securing the application due to revoked authentication"
        )

        // Always erase configuration when securing the application
        appScope.launch {
            configurationService.erase()
        }

        // Don't navigate if we're already handling an error in AuthActivity
        // This prevents restarting the activity and going back to splash screen
        if (isHandlingError) {
            Clogger.d(TAG, "Skipping navigation - already handling error in AuthActivity")
            return
        }

        val navigationIntent = Intent(context, AuthActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        try {
            context.startActivity(navigationIntent)
        } catch (e: Exception) {
            Clogger.e(TAG, "Failed to navigate to AuthActivity", e)
        }
    }


    /**
     * Handler for Firebase authentication state changes.
     */
    override fun onAuthStateChanged(server: FirebaseAuth) {
        if (server.currentUser == null) {
            secureApplication()
        } else {
            // User is authenticated, initialize FCM token
            appScope.launch {
                try {
                    fcmTokenManager.initializeToken()
                } catch (e: Exception) {
                    Clogger.e(TAG, "Failed to initialize FCM token", e)
                }
            }
        }
    }


// --- Lifecycle


    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        checkAuthValidity()
    }
}