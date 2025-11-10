package com.mpieterse.stride.ui.animations

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavBackStackEntry

private const val FadeDuration = 220
private const val FadeDelay = 90
private const val SharedAxisDuration = 300

fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughEnter(): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = FadeDuration,
            delayMillis = FadeDelay,
            easing = FastOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.96f,
        animationSpec = tween(
            durationMillis = FadeDuration,
            delayMillis = FadeDelay,
            easing = FastOutSlowInEasing
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughExit(): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = FadeDuration,
            easing = FastOutLinearInEasing
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisForwardEnter(): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = LinearOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = LinearOutSlowInEasing
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisForwardExit(): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = FastOutLinearInEasing
        )
    ) + scaleOut(
        targetScale = 1.04f,
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = FastOutLinearInEasing
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisBackwardEnter(): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = FastOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 1.04f,
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = FastOutSlowInEasing
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.sharedAxisBackwardExit(): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = FastOutLinearInEasing
        )
    ) + scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(
            durationMillis = SharedAxisDuration,
            easing = FastOutLinearInEasing
        )
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideUpEnter(): EnterTransition {
    return slideInVertically(
        animationSpec = tween(durationMillis = SharedAxisDuration, easing = FastOutSlowInEasing),
        initialOffsetY = { it }
    ) + fadeIn(animationSpec = tween(durationMillis = SharedAxisDuration))
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideDownExit(): ExitTransition {
    return slideOutVertically(
        animationSpec = tween(durationMillis = SharedAxisDuration, easing = FastOutLinearInEasing),
        targetOffsetY = { it }
    ) + fadeOut(animationSpec = tween(durationMillis = SharedAxisDuration))
}


