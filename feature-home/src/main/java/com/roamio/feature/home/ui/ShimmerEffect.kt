package com.roamio.feature.home.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.roamio.core.R as CoreR

private const val SHIMMER_DURATION_MS = 1200

@Composable
private fun shimmerBaseColor(): Color {
    val sage = colorResource(CoreR.color.roamio_sage)
    val muted = colorResource(CoreR.color.roamio_muted)
    return Color(
        red = sage.red * 0.86f + muted.red * 0.14f,
        green = sage.green * 0.86f + muted.green * 0.14f,
        blue = sage.blue * 0.86f + muted.blue * 0.14f,
        alpha = 1f,
    )
}

@Composable
private fun shimmerHighlightColor(): Color {
    val chip = colorResource(CoreR.color.roamio_chip)
    val white = colorResource(CoreR.color.roamio_white)
    return Color(
        red = chip.red * 0.55f + white.red * 0.45f,
        green = chip.green * 0.55f + white.green * 0.45f,
        blue = chip.blue * 0.55f + white.blue * 0.45f,
        alpha = 0.72f,
    )
}

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
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )
    return Brush.linearGradient(
        colorStops = arrayOf(
            0f to baseColor,
            0.38f to baseColor,
            0.5f to highlightColor,
            0.62f to baseColor,
            1f to baseColor,
        ),
        start = Offset(progress * 800f - 400f, 0f),
        end = Offset(progress * 800f, 0f),
    )
}

/**
 * Box filled with a size-aware animated shimmer sweep.
 *
 * @param modifier Layout and shape for the placeholder.
 * @author udit
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
) {
    val base = shimmerBaseColor()
    val highlight = shimmerHighlightColor()
    val transition = rememberInfiniteTransition(label = "shimmer_box")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_box_progress",
    )
    Box(
        modifier = modifier.drawBehind {
            drawRect(base)
            val band = size.width.coerceAtLeast(size.height) * 0.65f
            val startX = progress * (size.width + band * 2f) - band
            drawRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.32f to highlight.copy(alpha = highlight.alpha * 0.45f),
                        0.5f to highlight,
                        0.68f to highlight.copy(alpha = highlight.alpha * 0.45f),
                        1f to Color.Transparent,
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + band, size.height),
                ),
            )
        },
    )
}
