package com.mpieterse.stride.ui.layout.startup.roots

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.mpieterse.stride.ui.layout.startup.viewmodels.AuthViewModel
import com.mpieterse.stride.ui.layout.startup.views.AuthLaunchScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthSignInScreen
import com.mpieterse.stride.ui.layout.startup.views.AuthSignUpScreen
import com.mpieterse.stride.ui.animations.fadeThroughEnter
import com.mpieterse.stride.ui.animations.fadeThroughExit
import com.mpieterse.stride.ui.animations.sharedAxisBackwardEnter
import com.mpieterse.stride.ui.animations.sharedAxisBackwardExit
import com.mpieterse.stride.ui.animations.sharedAxisForwardEnter
import com.mpieterse.stride.ui.animations.sharedAxisForwardExit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthNavGraph(
    onGoToHomeActivity: () -> Unit,
    modifier: Modifier  = Modifier,
    authViewModel: AuthViewModel
) {
    // NavController will persist as long as AuthNavGraph stays in composition
    // Since we keep AuthNavGraph for Unauthenticated, Loading, and Error states,
    // the NavController won't be recreated when authState changes to Loading/Error
    val controller = rememberAnimatedNavController()
    
    AnimatedNavHost(
        navController = controller,
        startDestination = AuthScreen.Launch.route,
        enterTransition = { fadeThroughEnter() },
        exitTransition = { fadeThroughExit() },
        popEnterTransition = { fadeThroughEnter() },
        popExitTransition = { fadeThroughExit() }
    ) {
        composable(route = AuthScreen.Launch.route) {
            AuthLaunchScreen(
                modifier = modifier,
                onNavigateToSignIn = { controller.navigate(AuthScreen.SignIn.route) },
                onNavigateToSignUp = { controller.navigate(AuthScreen.SignUp.route) }
            )
        }
        composable(
            route = AuthScreen.SignIn.route,
            enterTransition = { sharedAxisForwardEnter() },
            exitTransition = { sharedAxisForwardExit() },
            popEnterTransition = { sharedAxisBackwardEnter() },
            popExitTransition = { sharedAxisBackwardExit() }
        ) {
            AuthSignInScreen(
                modifier = modifier,
                onSignIn = { authViewModel.signIn() },
                onGoogleSignIn = { authViewModel.googleSignIn() },
                viewModel = authViewModel
            )
        }
        composable(
            route = AuthScreen.SignUp.route,
            enterTransition = { sharedAxisForwardEnter() },
            exitTransition = { sharedAxisForwardExit() },
            popEnterTransition = { sharedAxisBackwardEnter() },
            popExitTransition = { sharedAxisBackwardExit() }
        ) {
            AuthSignUpScreen(
                modifier = modifier,
                onSignUp = { authViewModel.signUp() },
                onNavigateToSignIn = { controller.navigate(AuthScreen.SignIn.route) },
                viewModel = authViewModel
            )
        }
    }
}