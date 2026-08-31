package com.roamio.feature.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.roamio.feature.home.ui.motion.RoamioMotion

/**
 * Remote image with an animated shimmer placeholder until the requested URL loads.
 *
 * @param url Image URL.
 * @param contentDescription Accessibility label.
 * @param modifier Layout modifier.
 * @param contentScale Crop or fit mode.
 * @param onLoadedChange Called when Coil finishes loading the image.
 * @author udit
 */
@Composable
fun LoadingAsyncImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onLoadedChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    var loaded by remember(url) { mutableStateOf(false) }
    val imageAlpha by animateFloatAsState(
        targetValue = if (loaded) 1f else 0f,
        animationSpec = tween(RoamioMotion.IMAGE_FADE_MS),
        label = "image_fade",
    )
    LaunchedEffect(loaded) {
        onLoadedChange(loaded)
    }
    Box(modifier = modifier) {
        if (!loaded) {
            ShimmerBox(modifier = Modifier.fillMaxSize())
        }
        key(url) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCacheKey(url)
                    .diskCacheKey(url)
                    .crossfade(false)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = imageAlpha },
                contentScale = contentScale,
                onSuccess = { loaded = true },
                onError = { loaded = true },
            )
        }
    }
}
