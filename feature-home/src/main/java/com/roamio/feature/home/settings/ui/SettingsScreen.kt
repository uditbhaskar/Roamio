package com.roamio.feature.home.settings.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roamio.core.R as CoreR
import com.roamio.core.constants.CoreConstants
import com.roamio.feature.home.R
import com.roamio.feature.home.ui.motion.RoamioMotion
import com.roamio.feature.home.settings.viewModel.SettingsAction
import com.roamio.feature.home.settings.viewModel.SettingsUiState
import com.roamio.feature.home.settings.viewModel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

private val ExploreFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

/**
 * Presentational Settings UI driven by state and actions.
 *
 * @param state Current settings UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sage)
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 100.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                color = forest,
                fontFamily = ExploreFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
            )
            if (state.showSavedHint) {
                Text(
                    text = stringResource(R.string.settings_saved),
                    color = colorResource(CoreR.color.roamio_muted),
                    fontFamily = ExploreFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.settings_name), color = forest, fontFamily = ExploreFont)
        BasicTextField(
            value = state.displayName,
            onValueChange = { onAction(SettingsAction.NameChanged(it)) },
            textStyle = TextStyle(fontFamily = ExploreFont, fontSize = 16.sp, color = forest),
            decorationBox = { inner ->
                if (state.displayName.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_name_hint),
                        color = colorResource(CoreR.color.roamio_muted),
                        fontFamily = ExploreFont,
                    )
                }
                inner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(white)
                .padding(16.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = stringResource(R.string.settings_units), color = forest, fontFamily = ExploreFont)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitChip(
                label = stringResource(R.string.settings_celsius),
                selected = state.useCelsius,
                onClick = { onAction(SettingsAction.UseCelsius(true)) },
            )
            UnitChip(
                label = stringResource(R.string.settings_fahrenheit),
                selected = !state.useCelsius,
                onClick = { onAction(SettingsAction.UseCelsius(false)) },
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = stringResource(R.string.settings_currency), color = forest, fontFamily = ExploreFont)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CoreConstants.Currency.CODES.forEach { code ->
                UnitChip(
                    label = code,
                    selected = state.homeCurrency == code,
                    onClick = { onAction(SettingsAction.CurrencyChanged(code)) },
                )
            }
        }
    }
}

/**
 * Selectable pill for units or home currency.
 *
 * @param label Chip text.
 * @param selected Whether this option is active.
 * @param onClick Called when the chip is tapped.
 * @author udit
 */
@Composable
private fun UnitChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    val background by animateColorAsState(
        targetValue = if (selected) forest else white,
        animationSpec = RoamioMotion.chipTween,
        label = "unit_chip_bg",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) white else forest,
        animationSpec = RoamioMotion.chipTween,
        label = "unit_chip_fg",
    )
    Text(
        text = label,
        color = foreground,
        fontFamily = ExploreFont,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/**
 * Root composable that binds [SettingsViewModel].
 *
 * @author udit
 */
@Composable
fun SettingsScreenRoot() {
    val viewModel: SettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(state = uiState, onAction = viewModel::handleAction)
}

@Preview(name = "Loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenContentLoadedPreview() {
    MaterialTheme {
        SettingsScreenContent(
            state = SettingsUiState(displayName = "Morgan", useCelsius = true, homeCurrency = "USD"),
            onAction = {},
        )
    }
}

@Preview(name = "Saved hint", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenContentSavedPreview() {
    MaterialTheme {
        SettingsScreenContent(
            state = SettingsUiState(
                displayName = "Morgan",
                useCelsius = true,
                homeCurrency = "USD",
                showSavedHint = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenContentEmptyPreview() {
    MaterialTheme {
        SettingsScreenContent(state = SettingsUiState(), onAction = {})
    }
}
