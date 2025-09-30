package com.mpieterse.stride.ui.layout.startup.roots

/**
 * Logical alias for [AuthActivity] screen routes.
 */
sealed class AuthScreen(
    val route: String
) {
    object Launch : AuthScreen("launch")
    object Locked : AuthScreen("locked")
    object SignIn : AuthScreen("signIn")
    object SignUp : AuthScreen("signUp")
}