package com.roamio.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Remote image that cross-fades in over the shared shimmer placeholder.
 *
 * @param url Image URL.
 * @param contentDescription Accessibility label.
 * @param modifier Layout modifier.
 * @param contentScale Crop or fit mode.
 * @param placeholderColor Fallback tone when shimmer is off.
 * @param onLoadedChange Called when Coil finishes loading the image.
 * @author udit
 */
@Composable
fun LoadingAsyncImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color,
    onLoadedChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    var loaded by remember(url) { mutableStateOf(false) }
    LaunchedEffect(loaded) {
        onLoadedChange(loaded)
    }
    Box(modifier = modifier) {
        if (!loaded) {
            ShimmerBox(modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize().background(placeholderColor))
        }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .memoryCacheKey(url)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onSuccess = { loaded = true },
            onError = { loaded = true },
        )
    }
}
