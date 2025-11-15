package com.mpieterse.stride.ui.layout.shared.transitions

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.unit.IntOffset

/**
 * Centralized transition configuration for consistent animations throughout the app.
 * All transitions use standardized durations and easing for a smooth, cohesive experience.
 */
object TransitionConfig {
    // Standard durations (in milliseconds)
    const val FAST_DURATION = 200
    const val NORMAL_DURATION = 300
    const val SLOW_DURATION = 400

    // Fade transition timings
    val fadeInSpec = tween<Float>(durationMillis = NORMAL_DURATION)
    val fadeOutSpec = tween<Float>(durationMillis = FAST_DURATION)

    // Slide transition timings
    val slideInSpec = tween<IntOffset>(durationMillis = NORMAL_DURATION)
    val slideOutSpec = tween<IntOffset>(durationMillis = FAST_DURATION)

    /**
     * Standard fade transition for horizontal navigation (tabs, bottom nav)
     */
    fun horizontalFadeTransition(
    ): EnterTransition {
        return fadeIn(fadeInSpec)
    }

    fun horizontalFadeExit(
    ): ExitTransition {
        return fadeOut(fadeOutSpec)
    }

    /**
     * Slide transition for forward navigation (e.g., list -> detail)
     * Screen slides in from right, previous slides out to left
     */
    fun forwardSlideTransition(
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = slideInSpec
        ) + fadeIn(fadeInSpec)
    }

    fun forwardSlideExit(
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = slideOutSpec
        ) + fadeOut(fadeOutSpec)
    }

    /**
     * Slide transition for backward navigation (back button, pop)
     * Current screen slides out to right, previous slides in from left
     */
    fun backwardSlideTransition(
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = slideInSpec
        ) + fadeIn(fadeInSpec)
    }

    fun backwardSlideExit(
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = slideOutSpec
        ) + fadeOut(fadeOutSpec)
    }

    /**
     * Vertical slide transition for modal-like screens (e.g., bottom sheets, dialogs)
     */
    fun verticalSlideTransition(
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = slideInSpec
        ) + fadeIn(fadeInSpec)
    }

    fun verticalSlideExit(
    ): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { it },
            animationSpec = slideOutSpec
        ) + fadeOut(fadeOutSpec)
    }
}

