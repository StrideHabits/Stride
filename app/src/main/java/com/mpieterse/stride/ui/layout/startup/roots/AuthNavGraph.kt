package com.mpieterse.stride.ui.layout.startup.roots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel
import com.mpieterse.stride.ui.layout.startup.views.AuthLaunchScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthLockedScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthSignInScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthSignUpScreen

@Composable
fun AuthNavGraph(
    onGoToHomeActivity: () -> Unit,
    onTerminateCompose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = controller,
        startDestination = AuthScreen.Launch.route
    ) {
        // Launch
        composable(AuthScreen.Launch.route) {
            AuthLaunchScreen(
                modifier = modifier,
                model = authViewModel,
                onNavigateToSignIn = {
                    controller.navigate(AuthScreen.SignIn.route)
                },
                onNavigateToSignUp = {
                    controller.navigate(AuthScreen.SignUp.route)
                }
            )
        }

        // Locked
        composable(AuthScreen.Locked.route) {
            AuthLockedScreen(
                modifier = modifier,
                model = authViewModel,
                onSuccess = {
                    onGoToHomeActivity()
                    controller.popBackStack()
                },
                onDismiss = {
                    controller.navigate(AuthScreen.Launch.route)
                    controller.popBackStack()
                },
                onFailure = {
                    onTerminateCompose()
                }
            )
        }

        // SignIn
        composable(AuthScreen.SignIn.route) {
            AuthSignInScreen(
                modifier = modifier,
                model = authViewModel,
                onAuthenticated = {
                    controller.navigate(AuthScreen.Locked.route)
                },
            )
        }

        // SignUp
        composable(AuthScreen.SignUp.route) {
            AuthSignUpScreen(
                modifier = modifier,
                model = authViewModel,
                onAuthenticated = {
                    controller.navigate(AuthScreen.Locked.route)
                    controller.popBackStack()
                },
                onNavigateToSignIn = {
                    controller.navigate(AuthScreen.SignIn.route)
                }
            )
        }
    }
}
