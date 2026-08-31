package com.roamio.feature.home.popular.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roamio.core.photo.PhotoUrls
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.SeedCities
import com.roamio.core.places.SeedCity
import com.roamio.core.util.GeoUtils
import com.roamio.feature.home.R
import com.roamio.feature.home.popular.data.PopularCityCard
import com.roamio.feature.home.ui.motion.RoamioMotion
import com.roamio.feature.home.popular.viewModel.PopularAction
import com.roamio.feature.home.popular.viewModel.PopularFilter
import com.roamio.feature.home.popular.viewModel.PopularUiState
import com.roamio.feature.home.popular.viewModel.PopularViewModel
import com.roamio.feature.home.ui.LoadingAsyncImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import com.roamio.core.R as CoreR

private const val DECK_VISIBLE = 3
private const val DECK_SWIPE_FRACTION = 0.22f
private const val DECK_EXIT_MS = RoamioMotion.OVERLAY_MS
private const val DECK_ROTATION_DIVISOR = 48f

private val ExploreFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

/**
 * Presentational Popular UI driven by state and actions.
 *
 * @param state Current Popular UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun PopularScreenContent(
    state: PopularUiState,
    onAction: (PopularAction) -> Unit,
) {
    val cream = colorResource(CoreR.color.roamio_cream)
    val forest = colorResource(CoreR.color.roamio_forest)
    val visible = state.visibleCities
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cream)
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 100.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleIconButton(
                    icon = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.popular_cd_more),
                    onClick = { onAction(PopularAction.OpenSettings) },
                )
                CircleIconButton(
                    icon = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.popular_cd_saved),
                    onClick = { onAction(PopularAction.OpenSaved) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.popular_title),
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
            lineHeight = 44.sp,
            style = androidx.compose.ui.text.TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!state.isLoading && visible.isNotEmpty()) {
            FilterRow(
                selected = state.filter,
                onSelect = { onAction(PopularAction.SelectFilter(it)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        AnimatedContent(
            targetState = state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            transitionSpec = { RoamioMotion.crossfade() },
            label = "popular_load",
        ) { loading ->
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = forest, strokeWidth = 2.dp)
                    }
                }
                state.errorMessage != null && visible.isEmpty() -> {
                    Column {
                        Text(text = state.errorMessage, color = forest, fontFamily = ExploreFont)
                        TextButton(onClick = { onAction(PopularAction.Retry) }) {
                            Text(text = stringResource(R.string.popular_retry), color = forest)
                        }
                    }
                }
                visible.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.popular_empty),
                        color = forest,
                        fontFamily = ExploreFont,
                    )
                }
                else -> {
                    CityDeck(
                        cards = state.deckCities,
                        pageCount = visible.size,
                        onOpen = { card ->
                            onAction(PopularAction.OpenCity(card.city, state.filter, card.photoUrl))
                        },
                        onAdvance = { onAction(PopularAction.AdvanceDeck) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/**
 * Horizontal filter pills under the Popular title.
 *
 * @param selected Active filter chip.
 * @param onSelect Called when a chip is tapped.
 * @author udit
 */
@Composable
private fun FilterRow(
    selected: PopularFilter,
    onSelect: (PopularFilter) -> Unit,
) {
    val chips = listOf(
        Triple(PopularFilter.NEARBY, R.string.popular_filter_nearby, Icons.Filled.NearMe),
        Triple(PopularFilter.CLEAR_SKIES, R.string.popular_filter_clear, Icons.Outlined.WbSunny),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chips, key = { it.first }) { item ->
            val isSelected = item.first == selected
            val forest = colorResource(CoreR.color.roamio_forest)
            val chip = colorResource(CoreR.color.roamio_chip)
            FilterChip(
                label = stringResource(item.second),
                icon = item.third,
                selected = isSelected,
                onClick = { onSelect(item.first) },
                forest = forest,
                chip = chip,
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    forest: Color,
    chip: Color,
) {
    val white = colorResource(CoreR.color.roamio_white)
    val bg by animateColorAsState(
        targetValue = if (selected) forest else chip,
        animationSpec = RoamioMotion.chipTween,
        label = "filter_bg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) white else forest,
        animationSpec = RoamioMotion.chipTween,
        label = "filter_fg",
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            color = fg,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val chip = colorResource(CoreR.color.roamio_chip)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(chip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = forest,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CityDeck(
    cards: List<PopularCityCard>,
    pageCount: Int,
    onOpen: (PopularCityCard) -> Unit,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shown = cards.take(DECK_VISIBLE)
    if (shown.isEmpty()) return
    val peek = 28.dp
    val layers = shown.lastIndex
    val swipeX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val frontName = shown.first().city.name
    LaunchedEffect(frontName) {
        swipeX.snapTo(0f)
    }
    BoxWithConstraints(modifier = modifier) {
        val cardHeight = maxHeight - peek * layers
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        fun flingAway(direction: Float) {
            if (pageCount <= 1) return
            scope.launch {
                val target = direction * widthPx * 1.15f
                swipeX.animateTo(target, tween(DECK_EXIT_MS))
                onAdvance()
            }
        }
        shown.indices.reversed().forEach { depth ->
            val card = shown[depth]
            val isFront = depth == 0
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex((layers - depth).toFloat())
                    .fillMaxWidth()
                    .height(cardHeight)
                    .padding(horizontal = (depth * 10).dp)
                    .offset(y = -(peek * depth))
                    .graphicsLayer {
                        if (isFront) {
                            translationX = swipeX.value
                            rotationZ = swipeX.value / DECK_ROTATION_DIVISOR
                            alpha = 1f - (abs(swipeX.value) / widthPx) * 0.25f
                        }
                    }
                    .then(
                        if (isFront) {
                            Modifier.pointerInput(frontName, pageCount, widthPx) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    var dragged = false
                                    var current = swipeX.value
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        val dx = change.positionChange().x
                                        if (!dragged && abs(current + dx - swipeX.value) > viewConfiguration.touchSlop) {
                                            dragged = true
                                        }
                                        if (dragged) {
                                            change.consume()
                                            current += dx
                                            scope.launch { swipeX.snapTo(current) }
                                        }
                                        if (event.changes.none { it.pressed }) break
                                    }
                                    when {
                                        !dragged -> onOpen(card)
                                        pageCount > 1 && abs(swipeX.value) > widthPx * DECK_SWIPE_FRACTION -> {
                                            val direction = if (swipeX.value > 0f) 1f else -1f
                                            flingAway(direction)
                                        }
                                        else -> scope.launch {
                                            swipeX.animateTo(0f, spring())
                                        }
                                    }
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                if (isFront) {
                    DeckFrontCard(card = card)
                } else {
                    DeckPeekCard(
                        card = card,
                        onAdvance = { flingAway(-1f) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeckFrontCard(
    card: PopularCityCard,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp)),
    ) {
        DeckPhoto(card = card, contentDescription = stringResource(R.string.popular_cd_open))
        Text(
            text = countryLabel(card),
            color = Color.White,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 18.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, end = 22.dp, bottom = 22.dp),
        ) {
            Text(
                text = card.city.name,
                color = Color.White,
                fontFamily = ExploreFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                card.temperatureC?.let { temp ->
                    DeckMeta(
                        icon = Icons.Outlined.WbSunny,
                        label = stringResource(R.string.popular_weather, temp.roundToInt()),
                    )
                }
                DeckMeta(
                    icon = Icons.Outlined.NearMe,
                    label = stringResource(
                        R.string.popular_distance_km,
                        GeoUtils.formatKm(card.distanceKm),
                    ),
                )
            }
        }
    }
}

@Composable
private fun DeckPeekCard(
    card: PopularCityCard,
    onAdvance: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onAdvance),
    ) {
        DeckPhoto(card = card, contentDescription = stringResource(R.string.popular_cd_next))
        Text(
            text = countryLabel(card),
            color = Color.White,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 14.dp),
        )
    }
}

@Composable
private fun DeckPhoto(
    card: PopularCityCard,
    contentDescription: String,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val photoUrl = card.photoUrl.ifBlank {
        with(SeedCities) {
            PhotoUrls.display(
                card.city.photoFor(ActivityKind.POPULAR).ifBlank { genericPhoto(ActivityKind.POPULAR) },
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(forest))
        LoadingAsyncImage(
            url = photoUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x80041208),
                        0.45f to Color(0x33041208),
                        1f to Color(0xD9041208),
                    ),
                ),
        )
    }
}

@Composable
private fun DeckMeta(
    icon: ImageVector,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            color = Color.White,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )
    }
}

private fun countryLabel(card: PopularCityCard): String {
    return listOf(
        GeoUtils.countryFlag(card.city.countryCode),
        GeoUtils.countryName(card.city.countryCode).uppercase(),
    ).filter { it.isNotBlank() }.joinToString("  ")
}

/**
 * Root composable that binds [PopularViewModel] to the shell.
 *
 * @param onOpenHome Switch to the Home tab.
 * @param onOpenSaved Switch to the Saved tab.
 * @param onOpenSettings Switch to Settings.
 * @author udit
 */
@Composable
fun PopularScreenRoot(
    onOpenHome: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: PopularViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val openHome by viewModel.openHome.collectAsStateWithLifecycle()
    val openSaved by viewModel.openSaved.collectAsStateWithLifecycle()
    val openSettings by viewModel.openSettings.collectAsStateWithLifecycle()
    LaunchedEffect(openHome, openSaved, openSettings) {
        if (openHome) onOpenHome()
        if (openSaved) onOpenSaved()
        if (openSettings) onOpenSettings()
        if (openHome || openSaved || openSettings) viewModel.resetNavigation()
    }
    PopularScreenContent(
        state = uiState,
        onAction = { action ->
            viewModel.handleAction(action)
            if (action is PopularAction.OpenCity) {
                onOpenHome()
            }
        },
    )
}

private fun previewCard(name: String, code: String): PopularCityCard {
    val city = SeedCities.ALL.firstOrNull { it.name == name }
        ?: SeedCity(name, code, 60.0, 5.0)
    return PopularCityCard(
        city = city,
        temperatureC = 15.0,
        weatherCode = 0,
        photoUrl = with(SeedCities) {
            PhotoUrls.display(
                city.photoFor(ActivityKind.POPULAR).ifBlank { genericPhoto(ActivityKind.POPULAR) },
            )
        },
        distanceKm = 12.0,
    )
}

@Preview(name = "Deck front card", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun DeckFrontCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
        ) {
            DeckFrontCard(card = previewCard("Banff", "CA"))
        }
    }
}

@Preview(name = "Filter chip", showBackground = true, widthDp = 200)
@Composable
private fun FilterChipPreview() {
    MaterialTheme {
        FilterChip(
            label = stringResource(R.string.popular_filter_nearby),
            icon = Icons.Filled.NearMe,
            selected = true,
            onClick = {},
            forest = colorResource(CoreR.color.roamio_forest),
            chip = colorResource(CoreR.color.roamio_chip),
        )
    }
}

@Preview(name = "City deck", showBackground = true, widthDp = 360, heightDp = 520)
@Composable
private fun CityDeckPreview() {
    MaterialTheme {
        CityDeck(
            cards = listOf(
                previewCard("Banff", "CA"),
                previewCard("Innsbruck", "AT"),
                previewCard("Bergen", "NO"),
            ),
            pageCount = 3,
            onOpen = {},
            onAdvance = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(horizontal = 22.dp),
        )
    }
}

@Preview(name = "Loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PopularScreenContentLoadedPreview() {
    MaterialTheme {
        PopularScreenContent(
            state = PopularUiState(
                isLoading = false,
                cities = listOf(
                    previewCard("Banff", "CA"),
                    previewCard("Innsbruck", "AT"),
                    previewCard("Interlaken", "CH"),
                    previewCard("Bergen", "NO"),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PopularScreenContentLoadingPreview() {
    MaterialTheme {
        PopularScreenContent(state = PopularUiState(), onAction = {})
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PopularScreenContentErrorPreview() {
    MaterialTheme {
        PopularScreenContent(
            state = PopularUiState(
                isLoading = false,
                errorMessage = com.roamio.core.constants.CoreConstants.Errors.NETWORK,
            ),
            onAction = {},
        )
    }
}
