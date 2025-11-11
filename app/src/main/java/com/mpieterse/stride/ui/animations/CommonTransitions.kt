package com.mpieterse.stride.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Common animation durations
private const val ShortDuration = 200
private const val MediumDuration = 300
private const val LongDuration = 400

// Common easing curves
private val StandardEasing = FastOutSlowInEasing
private val DecelerateEasing = LinearOutSlowInEasing
private val AccelerateEasing = FastOutLinearInEasing

/**
 * Content size animation for dialogs
 */
@Composable
fun Modifier.dialogContentSizeTransition(): Modifier = this.then(
    Modifier.animateContentSize(
        animationSpec = tween(
            durationMillis = MediumDuration,
            easing = StandardEasing
        )
    )
)

/**
 * Fade in animation for content appearing
 */
fun fadeInTransition(duration: Int = MediumDuration) = fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
) + scaleIn(
    initialScale = 0.9f,
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
)

/**
 * Fade out animation for content disappearing
 */
fun fadeOutTransition(duration: Int = ShortDuration) = fadeOut(
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
) + scaleOut(
    targetScale = 0.9f,
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
)

/**
 * Slide in from bottom for FABs and bottom sheets
 */
fun slideInFromBottom(duration: Int = MediumDuration) = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(
        durationMillis = duration,
        easing = DecelerateEasing
    )
) + fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
)

/**
 * Slide out to bottom
 */
fun slideOutToBottom(duration: Int = ShortDuration) = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
) + fadeOut(
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
)

/**
 * Slide in from top
 */
fun slideInFromTop(duration: Int = MediumDuration) = slideInVertically(
    initialOffsetY = { -it },
    animationSpec = tween(
        durationMillis = duration,
        easing = DecelerateEasing
    )
) + fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
)

/**
 * Slide out to top
 */
fun slideOutToTop(duration: Int = ShortDuration) = slideOutVertically(
    targetOffsetY = { -it },
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
) + fadeOut(
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
)

/**
 * Scale and fade for buttons and interactive elements
 */
fun scaleInTransition(duration: Int = ShortDuration) = scaleIn(
    initialScale = 0.8f,
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
) + fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
)

/**
 * Expand vertically animation with fade
 */
fun expandVerticallyTransition(duration: Int = MediumDuration) = expandVertically(
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
) + fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        easing = StandardEasing
    )
)

/**
 * Shrink vertically animation with fade
 */
fun shrinkVerticallyTransition(duration: Int = ShortDuration) = shrinkVertically(
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
) + fadeOut(
    animationSpec = tween(
        durationMillis = duration,
        easing = AccelerateEasing
    )
)

