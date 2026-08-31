package com.roamio.feature.home.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Kayaking
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.SearchHit
import com.roamio.core.util.GeoUtils
import com.roamio.feature.home.R
import com.roamio.feature.home.viewModel.HomeAction
import com.roamio.feature.home.viewModel.HomeUiState
import com.roamio.feature.home.viewModel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt
import com.roamio.core.R as CoreR

private const val CHIP_TO_HERO_SPACING_DP = HOME_CHIP_TO_HERO_SPACING_DP

private val ExploreFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

private val TightHeadline = TextStyle(
    fontFamily = ExploreFont,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 40.sp,
    lineHeight = 42.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

/**
 * Presentational Home UI driven by state and actions.
 *
 * @param state Current Home UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val ink = colorResource(CoreR.color.roamio_ink)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sage),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 100.dp),
        ) {
            if (state.showsFullSkeleton) {
                HomeLoadingSkeleton(modifier = Modifier.fillMaxSize())
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    HomeLoadedContent(
                        state = state,
                        ink = ink,
                        onAction = onAction,
                    )
                }
            }
        }
        if (state.isSearchOpen) {
            SearchOverlay(
                query = state.searchQuery,
                results = state.searchResults,
                errorMessage = state.searchErrorMessage,
                onQuery = { onAction(HomeAction.QueryChanged(it)) },
                onSelect = { onAction(HomeAction.SelectSearch(it)) },
                onClose = { onAction(HomeAction.CloseSearch) },
            )
        }
    }
}

/**
 * True while Home should show the full-screen shimmer skeleton.
 *
 * @author udit
 */
private val HomeUiState.showsFullSkeleton: Boolean
    get() = isLoading || (featured == null && errorMessage == null)

@Composable
private fun ColumnScope.HomeLoadedContent(
    state: HomeUiState,
    ink: Color,
    onAction: (HomeAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val name = state.greetingName.ifBlank { stringResource(R.string.home_default_name) }
        Text(
            text = stringResource(R.string.home_greeting, name),
            color = ink,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WeatherChip(
                temperatureC = state.temperature,
                weatherCode = state.weatherCode,
                useCelsius = state.useCelsius,
            )
            HomeCurrencyChip(code = state.homeCurrency)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val flag = GeoUtils.countryFlag(state.countryCode)
            val country = state.countryName.uppercase()
            if (country.isNotBlank()) {
                Text(
                    text = listOf(flag, country).filter { it.isNotBlank() }.joinToString(" "),
                    color = ink,
                    fontFamily = ExploreFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 1.4.sp,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (state.headlineCity.isNotBlank()) {
                Text(
                    text = state.headlineCity,
                    style = TightHeadline,
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (state.selectedActivity != ActivityKind.PLACE) {
                Text(
                    text = headlineNoun(state.selectedActivity),
                    style = TightHeadline,
                    color = ink,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        SearchPill(onClick = { onAction(HomeAction.OpenSearch) })
    }
    Spacer(modifier = Modifier.height(10.dp))
    ActivityChips(
        selected = state.selectedActivity,
        loadedActivities = state.loadedActivities,
        onSelect = { onAction(HomeAction.SelectActivity(it)) },
    )
    Spacer(modifier = Modifier.height(CHIP_TO_HERO_SPACING_DP.dp))
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.TopCenter,
    ) {
        val heroModifier = Modifier.heroCircleSize(maxWidth, maxHeight)
        when {
            state.featured != null -> {
                FeaturedHero(
                    place = state.featured,
                    onStartTrip = { onAction(HomeAction.StartTrip) },
                    modifier = heroModifier,
                )
            }
            state.errorMessage != null -> {
                FeaturedMessage(
                    text = state.errorMessage,
                    actionLabel = stringResource(R.string.home_retry),
                    onAction = { onAction(HomeAction.Retry) },
                    modifier = heroModifier,
                )
            }
            else -> {
                HeroLoadingPlaceholder(modifier = heroModifier)
            }
        }
    }
}

private fun Modifier.heroCircleSize(maxWidth: Dp, maxHeight: Dp): Modifier {
    return size(homeHeroCircleSize(maxWidth, maxHeight))
}

/**
 * Second headline word for the selected activity chip.
 *
 * @param activity Active Hiking, Kayaking, or Biking filter.
 * @return Localized noun shown under the city name.
 * @author udit
 */
@Composable
private fun headlineNoun(activity: ActivityKind): String {
    val res = when (activity) {
        ActivityKind.PLACE -> R.string.home_headline_explore
        ActivityKind.POPULAR -> R.string.home_headline_popular
        ActivityKind.CAFE -> R.string.home_headline_cafe
        ActivityKind.HIKING -> R.string.home_headline_trails
        ActivityKind.KAYAKING -> R.string.home_headline_waters
        ActivityKind.BIKING -> R.string.home_headline_rides
    }
    return stringResource(res)
}

@Composable
private fun HomeCurrencyChip(
    code: String,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    Row(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(22.dp), clip = false)
            .background(white, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_currency_chip, code),
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )
    }
}

/**
 * Glass weather pill shown on Home and reused visually on detail.
 *
 * @param temperatureC Celsius temperature, or null when unknown.
 * @param weatherCode Open-Meteo code used to pick sun or cloud.
 * @param useCelsius Whether to show Celsius.
 * @author udit
 */
@Composable
fun WeatherChip(
    temperatureC: Double?,
    weatherCode: Int?,
    useCelsius: Boolean,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    val label = when {
        temperatureC == null -> stringResource(R.string.home_weather_unavailable)
        useCelsius -> stringResource(R.string.home_weather, temperatureC.roundToInt())
        else -> stringResource(R.string.home_weather_f, (temperatureC * 9 / 5 + 32).roundToInt())
    }
    val sunny = weatherCode == null || weatherCode <= 2
    Row(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(22.dp), clip = false)
            .background(white, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = if (sunny) Icons.Outlined.WbSunny else Icons.Filled.Cloud,
            contentDescription = null,
            tint = colorResource(CoreR.color.roamio_sun),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )
    }
}

/**
 * Vertical white search pill to the right of the Home title.
 *
 * @param onClick Opens the city search overlay.
 * @author udit
 */
@Composable
private fun SearchPill(onClick: () -> Unit) {
    val white = colorResource(CoreR.color.roamio_white)
    val forest = colorResource(CoreR.color.roamio_forest)
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(118.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp), clip = false)
            .clip(RoundedCornerShape(28.dp))
            .background(white)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.home_search_cd),
            tint = forest,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Popular, Cafe, Hiking, Kayaking, and Biking chips. Available chips lead; unavailable ones are greyed out at the end.
 *
 * @param selected Currently active activity filter on Home.
 * @param loadedActivities Chips that resolved live data for the current city.
 * @param onSelect Called when an available chip is tapped.
 * @author udit
 */
@Composable
private fun ActivityChips(
    selected: ActivityKind,
    loadedActivities: Set<ActivityKind>,
    onSelect: (ActivityKind) -> Unit,
) {
    val items = listOf(
        Triple(ActivityKind.POPULAR, R.string.home_activity_popular, Icons.Filled.GridView),
        Triple(ActivityKind.CAFE, R.string.home_activity_cafe, Icons.Filled.LocalCafe),
        Triple(ActivityKind.HIKING, R.string.home_activity_hiking, Icons.Filled.Terrain),
        Triple(ActivityKind.KAYAKING, R.string.home_activity_kayaking, Icons.Filled.Kayaking),
        Triple(ActivityKind.BIKING, R.string.home_activity_biking, Icons.AutoMirrored.Filled.DirectionsBike),
    )
    val ordered = items.partition { it.first in loadedActivities }.let { (ready, waiting) ->
        ready + waiting
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(ordered, key = { it.first }) { item ->
            val enabled = item.first in loadedActivities
            val isSelected = enabled && item.first == selected && item.first !in setOf(
                ActivityKind.CAFE,
                ActivityKind.POPULAR,
            )
            ActivityChip(
                label = stringResource(item.second),
                icon = item.third,
                selected = isSelected,
                enabled = enabled,
                onClick = { onSelect(item.first) },
            )
        }
    }
}

/**
 * One activity chip in selected, idle, or disabled chrome.
 *
 * @param label Chip text from strings.
 * @param icon Leading activity icon.
 * @param selected Whether this chip is the active filter.
 * @param enabled False when live data is unavailable for this chip.
 * @param onClick Called when the chip is tapped.
 * @author udit
 */
@Composable
private fun ActivityChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val chip = colorResource(CoreR.color.roamio_chip)
    val muted = colorResource(CoreR.color.roamio_muted)
    val bg = when {
        !enabled -> chip.copy(alpha = 0.45f)
        selected -> forest
        else -> chip
    }
    val fg = when {
        !enabled -> muted
        selected -> colorResource(CoreR.color.roamio_white)
        else -> forest
    }
    Row(
        modifier = Modifier
            .shadow(if (selected && enabled) 0.dp else 4.dp, RoundedCornerShape(22.dp), clip = false)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(
            text = label,
            color = fg,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

/**
 * Circular featured place card with Start Trip.
 *
 * @param place Place shown on the hero.
 * @param onStartTrip Opens place detail.
 * @param modifier Square size from the Home layout.
 * @author udit
 */
@Composable
private fun FeaturedHero(
    place: ExplorePlace,
    onStartTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val white = colorResource(CoreR.color.roamio_white)
    val forest = colorResource(CoreR.color.roamio_forest)
    val sage = colorResource(CoreR.color.roamio_sage)
    var heroReady by remember(place.photoUrl ?: place.osmId) { mutableStateOf(false) }
    LaunchedEffect(place.photoUrl ?: place.osmId) {
        heroReady = false
    }
    BoxWithConstraints(modifier = modifier.clip(CircleShape)) {
        val density = LocalDensity.current
        val centerPx = with(density) {
            Offset(maxWidth.toPx() / 2f, maxHeight.toPx() / 2f)
        }
        val edgeRadius = with(density) {
            maxOf(maxWidth, maxHeight).toPx() * 0.56f
        }
        key(place.photoUrl ?: place.osmId) {
            val heroPhoto = place.photoUrl?.takeIf { it.isNotBlank() }
            if (heroPhoto != null) {
                LoadingAsyncImage(
                    url = heroPhoto,
                    contentDescription = stringResource(R.string.home_cd_featured),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholderColor = forest,
                    onLoadedChange = { heroReady = it },
                )
            } else {
                ShimmerBox(modifier = Modifier.fillMaxSize())
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.42f to Color.Transparent,
                        0.78f to sage.copy(alpha = 0.28f),
                        1f to sage.copy(alpha = 0.96f),
                        center = centerPx,
                        radius = edgeRadius,
                    ),
                ),
        )
        if (heroReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1f to Color(0xCC041208),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = place.name,
                    color = white,
                    fontFamily = ExploreFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                if (place.blurb.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = place.blurb,
                        color = white.copy(alpha = 0.92f),
                        fontFamily = ExploreFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
                val distance = place.distanceKm
                if (distance != null || place.isOpenHours) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (place.isOpenHours) {
                            MetaItem(
                                icon = Icons.Outlined.Schedule,
                                text = stringResource(R.string.home_meta_open),
                            )
                        }
                        if (distance != null) {
                            MetaItem(
                                icon = Icons.Outlined.NearMe,
                                text = stringResource(R.string.home_distance_km, GeoUtils.formatKm(distance)),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(white.copy(alpha = 0.14f))
                        .border(1.4.dp, white.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                        .clickable(onClick = onStartTrip)
                        .padding(horizontal = 26.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_start_trip),
                        color = white,
                        fontFamily = ExploreFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

/**
 * Icon-plus-label meta slot on the featured hero.
 *
 * @param icon Leading meta icon.
 * @param text Distance or hours label.
 * @author udit
 */
@Composable
private fun MetaItem(icon: ImageVector, text: String) {
    val white = colorResource(CoreR.color.roamio_white)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = white, modifier = Modifier.size(13.dp))
        Text(
            text = text,
            color = white,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
    }
}

/**
 * Circular shimmer shown while the hero place is still resolving.
 *
 * @param modifier Square size from the Home layout.
 * @author udit
 */
@Composable
private fun HeroLoadingPlaceholder(modifier: Modifier = Modifier) {
    HeroContentShimmer(modifier = modifier)
}

/**
 * Circular empty or error stand-in for the featured hero.
 *
 * @param text Message shown in the circle.
 * @param actionLabel Retry button label.
 * @param onAction Called when retry is tapped.
 * @param modifier Square size from the Home layout.
 * @author udit
 */
@Composable
private fun FeaturedMessage(
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(colorResource(CoreR.color.roamio_chip)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 44.dp),
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onAction) {
            Text(text = actionLabel, color = forest, fontFamily = ExploreFont)
        }
    }
}

/**
 * Full-screen city search overlay on Home.
 *
 * @param query Current search text.
 * @param results Nominatim hits.
 * @param errorMessage Search-only error copy.
 * @param onQuery Called as the query changes.
 * @param onSelect Called when a hit is chosen.
 * @param onClose Dismisses the overlay.
 * @author udit
 */
@Composable
private fun SearchOverlay(
    query: String,
    results: List<SearchHit>,
    errorMessage: String?,
    onQuery: (String) -> Unit,
    onSelect: (SearchHit) -> Unit,
    onClose: () -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sage)
            .statusBarsPadding()
            .padding(22.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(white)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.home_search_hint),
                            color = colorResource(CoreR.color.roamio_muted),
                            fontFamily = ExploreFont,
                            fontSize = 14.sp,
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQuery,
                        textStyle = TextStyle(
                            fontFamily = ExploreFont,
                            fontSize = 14.sp,
                            color = forest,
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.home_search_close),
                    tint = forest,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onClose),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = forest,
                    fontFamily = ExploreFont,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            results.forEach { hit ->
                val country = GeoUtils.countryName(hit.countryCode)
                Text(
                    text = listOf(hit.name, country).filter { it.isNotBlank() }.distinct().joinToString(", "),
                    color = forest,
                    fontFamily = ExploreFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(hit) }
                        .padding(vertical = 12.dp),
                )
            }
        }
    }
}

/**
 * Root composable that binds [HomeViewModel] and requests location.
 *
 * @param onOpenPlace Called when Start Trip should open detail.
 * @author udit
 */
@Composable
fun HomeScreenRoot(
    onOpenPlace: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToPlace by viewModel.navigateToPlace.collectAsStateWithLifecycle()
    var askedPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) {
            viewModel.handleAction(HomeAction.LocationGranted)
        } else {
            viewModel.handleAction(HomeAction.LocationDenied)
        }
    }

    LaunchedEffect(Unit) {
        if (!askedPermission) {
            askedPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
        viewModel.handleAction(HomeAction.BecameVisible)
    }
    LaunchedEffect(navigateToPlace) {
        if (navigateToPlace) {
            onOpenPlace()
            viewModel.resetNavigation()
        }
    }
    HomeScreenContent(state = uiState, onAction = viewModel::handleAction)
}

private fun previewPlace(): ExplorePlace {
    return ExplorePlace(
        osmId = 1,
        osmType = "node",
        name = "The Sounds of Nature",
        latitude = 60.39,
        longitude = 5.32,
        activity = ActivityKind.PLACE,
        distanceKm = 10.0,
        blurb = "Walk quiet spruce trails above the fjord, then sit with the wind in the canopy.",
        countryCode = "NO",
        countryName = "Norway",
        isOpenHours = true,
    )
}

@Preview(name = "Loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenContentLoadedPreview() {
    MaterialTheme {
        HomeScreenContent(
            state = HomeUiState(
                isLoading = false,
                greetingName = "Morgan",
                countryCode = "NO",
                countryName = "Norway",
                headlineCity = "Nature",
                temperature = 15.0,
                weatherCode = 0,
                featured = previewPlace(),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenContentLoadingPreview() {
    MaterialTheme {
        HomeScreenContent(
            state = HomeUiState(
                isLoading = true,
                greetingName = "Morgan",
                countryCode = "NO",
                countryName = "Norway",
                headlineCity = "Nature",
                temperature = 15.0,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenContentErrorPreview() {
    MaterialTheme {
        HomeScreenContent(
            state = HomeUiState(
                isLoading = false,
                errorMessage = com.roamio.core.constants.CoreConstants.Errors.NETWORK,
                greetingName = "Morgan",
                countryCode = "NO",
                countryName = "Norway",
                headlineCity = "Nature",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Weather loaded", showBackground = true)
@Composable
private fun WeatherChipLoadedPreview() {
    MaterialTheme {
        WeatherChip(temperatureC = 15.0, weatherCode = 0, useCelsius = true)
    }
}

@Preview(name = "Weather empty", showBackground = true)
@Composable
private fun WeatherChipEmptyPreview() {
    MaterialTheme {
        WeatherChip(temperatureC = null, weatherCode = null, useCelsius = true)
    }
}
