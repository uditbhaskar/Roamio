package com.roamio.feature.home.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Shared motion timings and springs used across explore screens.
 *
 * @author udit
 */
object RoamioMotion {
    const val TAB_FADE_IN_MS = 220
    const val TAB_FADE_OUT_MS = 180
    const val SCREEN_MS = 320
    const val MAIN_ENTER_MS = 400
    const val CHIP_MS = 200
    const val IMAGE_FADE_MS = 280
    const val OVERLAY_MS = 260

    val bouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    val bouncySpringDp = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    val tabFadeIn = tween<Float>(TAB_FADE_IN_MS)
    val tabFadeOut = tween<Float>(TAB_FADE_OUT_MS)
    val screenTween = tween<Float>(SCREEN_MS, easing = FastOutSlowInEasing)
    val mainEnterTween = tween<Float>(MAIN_ENTER_MS, easing = FastOutSlowInEasing)
    val chipTween = tween<Color>(CHIP_MS, easing = FastOutSlowInEasing)

    /**
     * Horizontal tab transition that slides in the direction of the tab change.
     *
     * @param forward True when moving to a higher tab index.
     * @return Enter/exit transform for [androidx.compose.animation.AnimatedContent].
     * @author udit
     */
    fun tabSlide(forward: Boolean): ContentTransform {
        val direction = if (forward) 1 else -1
        return (slideInHorizontally(
            initialOffsetX = { direction * it / 5 },
            animationSpec = tween(TAB_FADE_IN_MS, easing = FastOutSlowInEasing),
        ) + fadeIn(tabFadeIn)) togetherWith (
            slideOutHorizontally(
                targetOffsetX = { -direction * it / 5 },
                animationSpec = tween(TAB_FADE_OUT_MS),
            ) + fadeOut(tabFadeOut)
            )
    }

    /**
     * Standard fade used when swapping loading and loaded content.
     *
     * @author udit
     */
    fun crossfade(): ContentTransform {
        return fadeIn(tabFadeIn) togetherWith fadeOut(tabFadeOut)
    }

    /**
     * Overlay enter from the top with a light fade.
     *
     * @author udit
     */
    fun overlayEnterTransition(): EnterTransition {
        return fadeIn(tween(OVERLAY_MS)) + slideInVertically(
            initialOffsetY = { -it / 3 },
            animationSpec = tween(OVERLAY_MS, easing = FastOutSlowInEasing),
        )
    }

    /**
     * Overlay exit toward the top with a light fade.
     *
     * @author udit
     */
    fun overlayExitTransition(): ExitTransition {
        return fadeOut(tabFadeOut) + slideOutVertically(
            targetOffsetY = { -it / 3 },
            animationSpec = tween(TAB_FADE_OUT_MS),
        )
    }

    /**
     * Hero/content reveal from below.
     *
     * @author udit
     */
    fun revealUpTransition(): EnterTransition {
        return fadeIn(tabFadeIn) + slideInVertically(
            initialOffsetY = { it / 6 },
            animationSpec = tween(OVERLAY_MS, easing = FastOutSlowInEasing),
        )
    }
}

/**
 * Bouncy press scale paired with the interaction source for [androidx.compose.foundation.clickable].
 *
 * @param pressedScale Scale applied while pressed.
 * @return Interaction source and scale modifier for the same control.
 * @author udit
 */
@Composable
fun rememberBouncyPressScale(pressedScale: Float = 0.96f): Pair<MutableInteractionSource, Modifier> {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = RoamioMotion.bouncySpring,
        label = "press_scale",
    )
    return interactionSource to Modifier.scale(scale)
}
