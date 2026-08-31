package com.roamio.feature.home.place.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Kayaking
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.roamio.core.R as CoreR
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.NearbyBite
import com.roamio.core.util.GeoUtils
import com.roamio.core.weather.CurrentWeather
import com.roamio.core.constants.CoreConstants
import com.roamio.feature.home.R
import com.roamio.feature.home.ui.motion.RoamioMotion
import com.roamio.feature.home.ui.LoadingAsyncImage
import com.roamio.feature.home.ui.ShimmerBox
import com.roamio.feature.home.place.viewModel.PlaceAction
import com.roamio.feature.home.place.viewModel.PlaceUiState
import com.roamio.feature.home.place.viewModel.PlaceViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

private val ExploreFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

private val TightTitle = TextStyle(
    fontFamily = ExploreFont,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 32.sp,
    lineHeight = 36.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private val TightHeadline = TextStyle(
    fontFamily = ExploreFont,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 26.sp,
    lineHeight = 32.sp,
    textAlign = TextAlign.Center,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private enum class PlacePhase {
    Loading,
    Error,
    Loaded,
}

/**
 * Presentational place detail UI driven by state and actions.
 *
 * @param state Current detail UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun PlaceScreenContent(
    state: PlaceUiState,
    onAction: (PlaceAction) -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    val phase = when {
        state.isLoading && state.place == null -> PlacePhase.Loading
        state.place == null -> PlacePhase.Error
        else -> PlacePhase.Loaded
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sage),
    ) {
        AnimatedContent(
            targetState = phase,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { RoamioMotion.crossfade() },
            label = "place_phase",
        ) { screen ->
            when (screen) {
                PlacePhase.Loading -> PlaceLoadingSkeleton(modifier = Modifier.fillMaxSize())
                PlacePhase.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.errorMessage ?: stringResource(R.string.place_missing),
                            color = forest,
                            fontFamily = ExploreFont,
                        )
                        TextButton(onClick = { onAction(PlaceAction.Retry) }) {
                            Text(text = stringResource(R.string.place_retry), color = forest)
                        }
                        TextButton(onClick = { onAction(PlaceAction.Back) }) {
                            Text(text = stringResource(R.string.place_cd_back), color = forest)
                        }
                    }
                }
                PlacePhase.Loaded -> {
                    PlaceLoaded(
                        state = state,
                        place = state.place!!,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceLoaded(
    state: PlaceUiState,
    place: ExplorePlace,
    onAction: (PlaceAction) -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    val chip = colorResource(CoreR.color.roamio_chip)
    val star = colorResource(CoreR.color.roamio_star)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(sage),
    ) {
        val heroHeight = maxHeight * 0.48f
        val sheetOverlap = 48.dp
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .align(Alignment.TopCenter),
            ) {
                key(place.photoUrl, place.osmId, place.city) {
                    val heroPhoto = place.photoUrl?.takeIf { it.isNotBlank() }
                    if (heroPhoto != null) {
                        LoadingAsyncImage(
                            url = heroPhoto,
                            contentDescription = place.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.4f to Color.Transparent,
                                1f to Color(0x99041208),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 64.dp),
                ) {
                    place.elevationMeters?.let { meters ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Terrain,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = stringResource(
                                    R.string.place_elevation_m,
                                    GeoUtils.formatElevation(meters),
                                ),
                                color = Color.White,
                                fontFamily = ExploreFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    Text(
                        text = place.name,
                        color = Color.White,
                        style = TightTitle,
                    )
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(heroHeight - sheetOverlap))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                            .background(chip)
                            .verticalScroll(scrollState)
                            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
                            .navigationBarsPadding(),
                    ) {
                        val country = GeoUtils.countryName(place.countryCode)
                            .ifBlank { place.countryName }
                        if (country.isNotBlank()) {
                            Text(
                                text = listOf(
                                    GeoUtils.countryFlag(place.countryCode),
                                    country.uppercase(),
                                ).filter { it.isNotBlank() }.joinToString("  "),
                                color = forest,
                                fontFamily = ExploreFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 2.6.sp,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Text(
                            text = place.name,
                            color = forest,
                            style = TightHeadline.copy(textAlign = TextAlign.Start),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AnimatedContent(
                            targetState = state.isEnriching,
                            transitionSpec = { RoamioMotion.crossfade() },
                            label = "place_blurb",
                        ) { enriching ->
                            if (enriching) {
                                PlaceBlurbShimmer(modifier = Modifier.fillMaxWidth())
                            } else if (place.blurb.isNotBlank()) {
                                Text(
                                    text = place.blurb,
                                    color = colorResource(CoreR.color.roamio_muted),
                                    fontFamily = ExploreFont,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        StatPills(place = place)
                        if (state.focusUnavailable && state.detailFocus != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FocusEmptyMessage(focus = state.detailFocus)
                        }
                        place.photoAttribution?.let { credit ->
                            Text(
                                text = stringResource(R.string.place_attribution, credit),
                                color = colorResource(CoreR.color.roamio_muted),
                                fontFamily = ExploreFont,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        if (place.activity != ActivityKind.CAFE) {
                            state.nearby?.let { nearby ->
                                Spacer(modifier = Modifier.height(16.dp))
                                NearbyCard(nearby = nearby)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-22).dp, y = (-28).dp)
                            .size(58.dp)
                            .shadow(12.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(white)
                            .clickable { onAction(PlaceAction.ToggleSave) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (state.isSaved) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(R.string.place_cd_save),
                            tint = if (state.isSaved) star else colorResource(CoreR.color.roamio_muted),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleChrome(onClick = { onAction(PlaceAction.Back) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.place_cd_back),
                    tint = Color.White,
                )
            }
            PlaceWeatherChip(weather = state.weather)
        }
    }
}

@Composable
private fun FocusEmptyMessage(focus: ActivityKind) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val message = when (focus) {
        ActivityKind.CAFE -> stringResource(R.string.place_no_cafe)
        ActivityKind.HIKING -> stringResource(R.string.place_no_activity, stringResource(R.string.place_activity_hiking))
        ActivityKind.KAYAKING -> stringResource(R.string.place_no_activity, stringResource(R.string.place_activity_kayaking))
        ActivityKind.BIKING -> stringResource(R.string.place_no_activity, stringResource(R.string.place_activity_biking))
        ActivityKind.POPULAR -> stringResource(R.string.place_no_activity, stringResource(R.string.place_activity_popular))
        ActivityKind.PLACE -> ""
    }
    if (message.isBlank()) return
    Text(
        text = message,
        color = forest,
        fontFamily = ExploreFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
}

@Composable
private fun CircleChrome(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.38f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun PlaceWeatherChip(weather: CurrentWeather?) {
    if (weather == null) return
    val label = stringResource(R.string.place_weather, weather.temperatureC.roundToInt())
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.38f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.WbSunny,
            contentDescription = null,
            tint = colorResource(CoreR.color.roamio_sun),
            modifier = Modifier.size(16.dp),
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

@Composable
private fun StatPills(place: ExplorePlace) {
    val white = colorResource(CoreR.color.roamio_white)
    val activityLabel = stringResource(
        when (place.activity) {
            ActivityKind.PLACE -> R.string.place_activity_explore
            ActivityKind.POPULAR -> R.string.place_activity_popular
            ActivityKind.CAFE -> R.string.place_activity_cafe
            ActivityKind.HIKING -> R.string.place_activity_hiking
            ActivityKind.KAYAKING -> R.string.place_activity_kayaking
            ActivityKind.BIKING -> R.string.place_activity_biking
        },
    )
    val activityIcon = when (place.activity) {
        ActivityKind.PLACE -> Icons.Filled.Place
        ActivityKind.POPULAR -> Icons.Filled.GridView
        ActivityKind.CAFE -> Icons.Filled.LocalCafe
        ActivityKind.HIKING -> Icons.Filled.Terrain
        ActivityKind.KAYAKING -> Icons.Filled.Kayaking
        ActivityKind.BIKING -> Icons.AutoMirrored.Filled.DirectionsBike
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatPill(
            icon = activityIcon,
            text = activityLabel,
            modifier = Modifier.weight(1f),
            background = white,
        )
        place.distanceKm?.let { km ->
            StatPill(
                icon = Icons.Outlined.Route,
                text = stringResource(R.string.place_distance_km, GeoUtils.formatKm(km)),
                modifier = Modifier.weight(1f),
                background = white,
            )
        }
        place.elevationMeters?.let { meters ->
            StatPill(
                icon = Icons.Outlined.Landscape,
                text = stringResource(R.string.place_elevation_m, GeoUtils.formatElevation(meters)),
                modifier = Modifier.weight(1f),
                background = white,
            )
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    text: String,
    modifier: Modifier,
    background: Color,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = forest,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NearbyCard(nearby: NearbyBite) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val muted = colorResource(CoreR.color.roamio_muted)
    val cream = colorResource(CoreR.color.roamio_cream)
    val cafeFallback = CoreConstants.Api.WIKIMEDIA_FILE_PATH + CoreConstants.Api.FALLBACK_CAFE_FILE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cream)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colorResource(CoreR.color.roamio_sage)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(nearby.photoUrl ?: cafeFallback)
                    .crossfade(true)
                    .build(),
                contentDescription = nearby.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nearby.name,
                color = forest,
                fontFamily = ExploreFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val line = listOf(nearby.address, nearby.cuisine).filter { it.isNotBlank() }
                .joinToString(" · ")
            if (line.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = muted,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = line,
                        color = muted,
                        fontFamily = ExploreFont,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            nearby.walkMinutes?.let { minutes ->
                Text(
                    text = stringResource(R.string.place_walk_min, minutes),
                    color = forest,
                    fontFamily = ExploreFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Root composable that binds [PlaceViewModel] and pops back.
 *
 * @param onBack Called when the user leaves detail.
 * @author udit
 */
@Composable
fun PlaceScreenRoot(
    onBack: () -> Unit,
) {
    val viewModel: PlaceViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateBack by viewModel.navigateBack.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.handleAction(PlaceAction.Open)
    }
    LaunchedEffect(navigateBack) {
        if (navigateBack) {
            onBack()
            viewModel.resetNavigation()
        }
    }
    PlaceScreenContent(state = uiState, onAction = viewModel::handleAction)
}

private fun previewPlace(): ExplorePlace {
    return ExplorePlace(
        osmId = 1,
        osmType = "node",
        name = "Tyrolean Alps",
        latitude = 47.2,
        longitude = 11.4,
        activity = ActivityKind.HIKING,
        elevationMeters = 2665,
        distanceKm = 10.0,
        blurb = "High limestone ridges and quiet alpine meadows above Innsbruck.",
        countryCode = "AT",
        countryName = "Austria",
    )
}

@Preview(name = "Loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlaceScreenContentLoadedPreview() {
    MaterialTheme {
        PlaceScreenContent(
            state = PlaceUiState(
                isLoading = false,
                place = previewPlace(),
                weather = CurrentWeather(15.0, 0),
                nearby = NearbyBite(
                    name = "Alpine Cafe",
                    address = "Altadena, Innsbruck",
                    cuisine = "cafe",
                    walkMinutes = 8,
                    latitude = 47.2,
                    longitude = 11.4,
                ),
                isSaved = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Loaded no nearby", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlaceScreenContentLoadedEmptyPreview() {
    MaterialTheme {
        PlaceScreenContent(
            state = PlaceUiState(
                isLoading = false,
                place = previewPlace(),
                weather = CurrentWeather(15.0, 0),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Enriching", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlaceScreenContentEnrichingPreview() {
    MaterialTheme {
        PlaceScreenContent(
            state = PlaceUiState(
                isLoading = false,
                isEnriching = true,
                place = previewPlace(),
                weather = CurrentWeather(15.0, 0),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlaceScreenContentLoadingPreview() {
    MaterialTheme {
        PlaceScreenContent(state = PlaceUiState(), onAction = {})
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlaceScreenContentErrorPreview() {
    MaterialTheme {
        PlaceScreenContent(
            state = PlaceUiState(
                isLoading = false,
                errorMessage = CoreConstants.Errors.NETWORK,
            ),
            onAction = {},
        )
    }
}
