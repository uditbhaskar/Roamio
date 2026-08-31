package com.roamio.feature.home.saved.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.roamio.core.R as CoreR
import com.roamio.core.places.SavedPlaceRecord
import com.roamio.core.util.GeoUtils
import com.roamio.feature.home.R
import com.roamio.feature.home.saved.viewModel.SavedAction
import com.roamio.feature.home.saved.viewModel.SavedUiState
import com.roamio.feature.home.saved.viewModel.SavedViewModel
import org.koin.androidx.compose.koinViewModel

private val ExploreFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

private val SavedCardShape = RoundedCornerShape(24.dp)

/**
 * Presentational Saved UI driven by state and actions.
 *
 * @param state Current Saved UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun SavedScreenContent(
    state: SavedUiState,
    onAction: (SavedAction) -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sage)
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 100.dp),
    ) {
        Text(
            text = stringResource(R.string.saved_title),
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (state.places.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.saved_empty),
                    color = forest,
                    fontFamily = ExploreFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(
                    items = state.places,
                    key = { record ->
                        record.saveKey.ifBlank { "${record.osmType}-${record.osmId}-${record.name}" }
                    },
                ) { record ->
                    SavedDismissibleCard(
                        record = record,
                        onOpen = { onAction(SavedAction.Open(record)) },
                        onDelete = { onAction(SavedAction.Delete(record)) },
                    )
                }
            }
        }
    }
}

/**
 * Saved row that swipes away to delete.
 *
 * @param record Persisted place shown on the card.
 * @param onOpen Called when the traveler opens the place.
 * @param onDelete Called when the traveler removes the place.
 * @author udit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedDismissibleCard(
    record: SavedPlaceRecord,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val deleteRed = colorResource(CoreR.color.roamio_delete)
    val white = colorResource(CoreR.color.roamio_white)
    val deleteLabel = stringResource(R.string.saved_delete)
    val haptics = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { distance -> distance * 0.35f },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(SavedCardShape)
                        .background(deleteRed)
                        .semantics { contentDescription = deleteLabel }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.saved_cd_delete),
                        tint = white,
                    )
                }
            }
        },
        content = {
            SavedCard(
                record = record,
                onClick = onOpen,
            )
        },
    )
}

/**
 * Photo card for one starred place in the Saved list.
 *
 * @param record Persisted place shown on the card.
 * @param onClick Called when the traveler opens the place.
 * @author udit
 */
@Composable
private fun SavedCard(
    record: SavedPlaceRecord,
    onClick: () -> Unit,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(SavedCardShape)
            .clickable(onClick = onClick),
    ) {
        if (record.photoUrl != null) {
            AsyncImage(
                model = record.photoUrl,
                contentDescription = stringResource(R.string.saved_cd_open),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(forest.copy(alpha = 0.5f), forest))),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color(0xB3041208))),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            Text(
                text = listOf(
                    GeoUtils.countryFlag(record.countryCode),
                    GeoUtils.countryName(record.countryCode),
                ).filter { it.isNotBlank() }.joinToString(" "),
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = ExploreFont,
                fontSize = 11.sp,
            )
            Text(
                text = record.name,
                color = Color.White,
                fontFamily = ExploreFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            )
        }
    }
}

/**
 * Root composable that binds [SavedViewModel].
 *
 * @param onOpenPlace Called when a saved card is opened.
 * @author udit
 */
@Composable
fun SavedScreenRoot(
    onOpenPlace: () -> Unit,
) {
    val viewModel: SavedViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToPlace by viewModel.navigateToPlace.collectAsStateWithLifecycle()
    LaunchedEffect(navigateToPlace) {
        if (navigateToPlace) {
            onOpenPlace()
            viewModel.resetNavigation()
        }
    }
    SavedScreenContent(state = uiState, onAction = viewModel::handleAction)
}

@Preview(name = "Loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SavedScreenContentLoadedPreview() {
    MaterialTheme {
        SavedScreenContent(
            state = SavedUiState(
                places = listOf(
                    SavedPlaceRecord(
                        osmId = 1,
                        osmType = "node",
                        name = "Fløyen",
                        latitude = 60.39,
                        longitude = 5.33,
                        countryCode = "NO",
                        activity = "HIKING",
                    ),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SavedScreenContentEmptyPreview() {
    MaterialTheme {
        SavedScreenContent(state = SavedUiState(), onAction = {})
    }
}
