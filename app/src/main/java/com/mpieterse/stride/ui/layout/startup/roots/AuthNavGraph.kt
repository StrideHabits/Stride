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

@Composable
fun AuthNavGraph(
    onGoToHomeActivity: () -> Unit,
    modifier: Modifier  = Modifier,
    authViewModel: AuthViewModel
) {
    // NavController will persist as long as AuthNavGraph stays in composition
    // Since we keep AuthNavGraph for Unauthenticated, Loading, and Error states,
    // the NavController won't be recreated when authState changes to Loading/Error
    val controller = rememberNavController()
    
    NavHost(navController = controller, startDestination = AuthScreen.Launch.route) {
        composable(route = AuthScreen.Launch.route) {
            AuthLaunchScreen(
                modifier = modifier,
                onNavigateToSignIn = { controller.navigate(AuthScreen.SignIn.route) },
                onNavigateToSignUp = { controller.navigate(AuthScreen.SignUp.route) }
            )
        }
        composable(route = AuthScreen.SignIn.route) {
            AuthSignInScreen(
                modifier = modifier,
                onSignIn = { authViewModel.signIn() },
                onGoogleSignIn = { authViewModel.googleSignIn() },
                viewModel = authViewModel
            )
        }
        composable(route = AuthScreen.SignUp.route) {
            AuthSignUpScreen(
                modifier = modifier,
                onSignUp = { authViewModel.signUp() },
                onNavigateToSignIn = { controller.navigate(AuthScreen.SignIn.route) },
                viewModel = authViewModel
            )
        }
    }
}