package com.roamio.feature.home.currency.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roamio.core.R as CoreR
import com.roamio.core.constants.CoreConstants
import com.roamio.feature.home.R
import com.roamio.feature.home.ui.motion.RoamioMotion
import com.roamio.feature.home.currency.viewModel.CurrencyAction
import com.roamio.feature.home.currency.viewModel.CurrencyUiState
import com.roamio.feature.home.currency.viewModel.CurrencyViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

private val ExploreFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

/**
 * Presentational Convert UI driven by state and actions.
 *
 * @param state Current convert UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun CurrencyScreenContent(
    state: CurrencyUiState,
    onAction: (CurrencyAction) -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    val cream = colorResource(CoreR.color.roamio_cream)
    val white = colorResource(CoreR.color.roamio_white)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sage)
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 100.dp),
    ) {
        Text(
            text = stringResource(R.string.currency_title),
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.currency_amount),
            color = forest,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
        )
        BasicTextField(
            value = state.amount,
            onValueChange = { onAction(CurrencyAction.AmountChanged(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(
                fontFamily = ExploreFont,
                fontSize = 22.sp,
                color = forest,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(white)
                .padding(16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.currency_from), color = forest, fontFamily = ExploreFont)
        CodeRow(
            selected = state.fromCode,
            onSelect = { onAction(CurrencyAction.FromChanged(it)) },
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.currency_to), color = forest, fontFamily = ExploreFont)
        CodeRow(
            selected = state.toCode,
            onSelect = { onAction(CurrencyAction.ToChanged(it)) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.currency_convert),
            color = white,
            fontFamily = ExploreFont,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(forest)
                .clickable { onAction(CurrencyAction.Convert) }
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        AnimatedContent(
            targetState = Triple(state.isLoading, state.errorMessage, state.result),
            transitionSpec = { RoamioMotion.crossfade() },
            label = "currency_result",
        ) { (loading, error, result) ->
            when {
                loading -> CircularProgressIndicator(color = forest, strokeWidth = 2.dp)
                error != null -> {
                    Column {
                        Text(text = error, color = forest, fontFamily = ExploreFont)
                        Text(
                            text = stringResource(R.string.currency_retry),
                            color = forest,
                            modifier = Modifier.clickable { onAction(CurrencyAction.Retry) },
                        )
                    }
                }
                result != null -> {
                    Text(
                        text = stringResource(
                            R.string.currency_result,
                            String.format(Locale.US, "%.2f", result),
                            state.toCode,
                        ),
                        color = forest,
                        fontFamily = ExploreFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(cream)
                            .padding(18.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * Row of ISO currency codes used as From or To.
 *
 * @param selected Currently chosen code.
 * @param onSelect Called when a code chip is tapped.
 * @author udit
 */
@Composable
private fun CodeRow(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val white = colorResource(CoreR.color.roamio_white)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CoreConstants.Currency.CODES.forEach { code ->
            val on = code == selected
            Text(
                text = code,
                color = if (on) white else forest,
                fontFamily = ExploreFont,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (on) forest else white)
                    .clickable { onSelect(code) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * Root composable that binds [CurrencyViewModel].
 *
 * @author udit
 */
@Composable
fun CurrencyScreenRoot() {
    val viewModel: CurrencyViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CurrencyScreenContent(state = uiState, onAction = viewModel::handleAction)
}

@Preview(name = "Loaded", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CurrencyScreenContentLoadedPreview() {
    MaterialTheme {
        CurrencyScreenContent(
            state = CurrencyUiState(result = 0.92),
            onAction = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CurrencyScreenContentLoadingPreview() {
    MaterialTheme {
        CurrencyScreenContent(
            state = CurrencyUiState(isLoading = true),
            onAction = {},
        )
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CurrencyScreenContentErrorPreview() {
    MaterialTheme {
        CurrencyScreenContent(
            state = CurrencyUiState(errorMessage = CoreConstants.Errors.NETWORK),
            onAction = {},
        )
    }
}
