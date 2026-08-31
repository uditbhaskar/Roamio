package com.roamio.feature.home.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.roamio.core.R as CoreR

private const val SHIMMER_DURATION_MS = 1200
private const val SHIMMER_TRAVEL = 900f

/**
 * Animated brush used for skeleton placeholders across Home and cards.
 *
 * @param baseColor Resting skeleton tone.
 * @param highlightColor Sweep highlight tone.
 * @return Linear gradient that moves on each frame.
 * @author udit
 */
@Composable
fun rememberShimmerBrush(
    baseColor: Color,
    highlightColor: Color,
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val travel by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_TRAVEL,
        animationSpec = infiniteRepeatable(
            animation = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_travel",
    )
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(travel - SHIMMER_TRAVEL, 0f),
        end = Offset(travel, 0f),
    )
}

/**
 * Default Roamio skeleton colors derived from theme chips.
 *
 * @author udit
 */
@Composable
fun rememberDefaultShimmerBrush(): Brush {
    val base = colorResource(CoreR.color.roamio_chip)
    val highlight = colorResource(CoreR.color.roamio_white)
    return rememberShimmerBrush(
        baseColor = base.copy(alpha = 0.72f),
        highlightColor = highlight.copy(alpha = 0.95f),
    )
}

/**
 * Box filled with the shared shimmer gradient.
 *
 * @param modifier Layout and shape for the placeholder.
 * @param brush Optional override; defaults to [rememberDefaultShimmerBrush].
 * @author udit
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    brush: Brush = rememberDefaultShimmerBrush(),
) {
    Box(modifier = modifier.background(brush))
}
