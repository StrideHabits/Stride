package com.mpieterse.stride.ui.layout.startup.roots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel
import com.mpieterse.stride.ui.layout.startup.views.AuthLaunchScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthSignInScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthSignUpScreen
import com.mpieterse.stride.ui.layout.shared.transitions.TransitionConfig

@Composable
fun AuthNavGraph(
    onGoToHomeActivity: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val controller = rememberNavController()

    NavHost(
        navController = controller,
        startDestination = AuthScreen.Launch.route,
        modifier = modifier
    ) {
        composable(
            route = AuthScreen.Launch.route,
            enterTransition = { TransitionConfig.horizontalFadeTransition() },
            exitTransition = { TransitionConfig.horizontalFadeExit() },
            popEnterTransition = { TransitionConfig.horizontalFadeTransition() },
            popExitTransition = { TransitionConfig.horizontalFadeExit() }
        ) {
            AuthLaunchScreen(
                modifier = Modifier,
                onNavigateToSignIn = { controller.navigate(AuthScreen.SignIn.route) },
                onNavigateToSignUp = { controller.navigate(AuthScreen.SignUp.route) }
            )
        }
        composable(
            route = AuthScreen.SignIn.route,
            enterTransition = { TransitionConfig.forwardSlideTransition() },
            exitTransition = { TransitionConfig.forwardSlideExit() },
            popEnterTransition = { TransitionConfig.backwardSlideTransition() },
            popExitTransition = { TransitionConfig.backwardSlideExit() }
        ) {
            AuthSignInScreen(
                modifier = Modifier,
                onSignIn = { authViewModel.signIn() },
                onGoogleSignIn = { authViewModel.googleSignIn() },
                viewModel = authViewModel
            )
        }
        composable(
            route = AuthScreen.SignUp.route,
            enterTransition = { TransitionConfig.forwardSlideTransition() },
            exitTransition = { TransitionConfig.forwardSlideExit() },
            popEnterTransition = { TransitionConfig.backwardSlideTransition() },
            popExitTransition = { TransitionConfig.backwardSlideExit() }
        ) {
            AuthSignUpScreen(
                modifier = Modifier,
                onSignUp = { authViewModel.signUp() },
                onNavigateToSignIn = { controller.navigate(AuthScreen.SignIn.route) },
                viewModel = authViewModel
            )
        }
    }
}