package com.roamio.feature.onboarding.ui

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.roamio.feature.onboarding.R
import com.roamio.feature.onboarding.viewModel.OnboardingAction
import com.roamio.feature.onboarding.viewModel.OnboardingUiState
import com.roamio.feature.onboarding.viewModel.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

private val OnboardingDisplayFont = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
)

private val OnboardingTightText = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

/**
 * Presentational onboarding UI driven by state and actions.
 *
 * @param state Current onboarding UI state.
 * @param onAction Callback invoked when the user performs an action.
 * @author udit
 */
@Composable
fun OnboardingScreenContent(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    OnboardingExplorePage(
        pageCount = state.pageCount,
        onGetStarted = { onAction(OnboardingAction.CONTINUE) },
    )
}

@Composable
private fun OnboardingExplorePage(
    pageCount: Int,
    onGetStarted: () -> Unit,
) {
    val teal = colorResource(R.color.onboarding_explore_teal)
    val ink = colorResource(R.color.onboarding_explore_ink)
    val onTeal = colorResource(R.color.onboarding_explore_on_teal)
    val privacy = colorResource(R.color.onboarding_explore_privacy)
    val dot = colorResource(R.color.onboarding_explore_dot)
    val pageDescription = stringResource(
        R.string.onboarding_page_indicator,
        1,
        pageCount,
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.onboarding_page1_hero),
            contentDescription = stringResource(R.string.onboarding_cd_hero),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colorResource(R.color.onboarding_explore_scrim),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            OnboardingPageDots(
                pageCount = pageCount,
                selectedColor = teal,
                unselectedColor = dot,
                pageDescription = pageDescription,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OnboardingHeadlineLockup(
                kicker = stringResource(R.string.onboarding_page1_kicker),
                firstLine = stringResource(R.string.onboarding_page1_title_line1),
                secondLine = stringResource(R.string.onboarding_page1_title_line2),
                color = ink,
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = teal,
                    contentColor = onTeal,
                ),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_get_started),
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    ),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                )
            }
            Text(
                text = stringResource(R.string.onboarding_privacy),
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = privacy,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun OnboardingHeadlineLockup(
    kicker: String,
    firstLine: String,
    secondLine: String,
    color: Color,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val context = LocalContext.current
    val extraBold = remember(context) {
        ResourcesCompat.getFont(context, R.font.poppins_extrabold)
    }
    val medium = remember(context) {
        ResourcesCompat.getFont(context, R.font.poppins_medium)
    }
    val kickerColor = colorResource(R.color.onboarding_explore_kicker)
    val strapColor = colorResource(R.color.onboarding_explore_strap)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxPx = constraints.maxWidth
        if (maxPx <= 0) {
            return@BoxWithConstraints
        }
        val longest = if (firstLine.length >= secondLine.length) firstLine else secondLine
        val titleSize = remember(longest, maxPx) {
            widthFitHeadlineSize(
                measurer = measurer,
                text = longest,
                maxPx = maxPx,
            )
        }
        val kickerSize = (titleSize * 0.50f).coerceIn(16f, 23f)
        val kickerStyle = OnboardingTightText.merge(
            TextStyle(
                fontFamily = OnboardingDisplayFont,
                fontWeight = FontWeight.Normal,
                fontSize = kickerSize.sp,
                lineHeight = kickerSize.sp,
            ),
        )
        val titleStyle = OnboardingTightText.merge(
            TextStyle(
                fontFamily = OnboardingDisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize.sp,
                lineHeight = titleSize.sp,
                letterSpacing = (-1.2).sp,
            ),
        )
        val kickerLayout = remember(kicker, kickerSize) {
            measurer.measure(
                text = kicker,
                style = kickerStyle,
                maxLines = 1,
                softWrap = false,
            )
        }
        val firstLayout = remember(firstLine, titleSize) {
            measurer.measure(
                text = firstLine,
                style = titleStyle,
                maxLines = 1,
                softWrap = false,
            )
        }
        val secondLayout = remember(secondLine, titleSize) {
            measurer.measure(
                text = secondLine,
                style = titleStyle,
                maxLines = 1,
                softWrap = false,
            )
        }
        val titleSizePx = with(density) { titleSize.sp.toPx() }
        val kickerSizePx = with(density) { kickerSize.sp.toPx() }
        val trackingPx = with(density) { (-1.2).sp.toPx() }
        val kickerInk = kickerLayout.paintInk(kicker, medium, kickerSizePx, 0f)
        val firstInk = firstLayout.paintInk(firstLine, extraBold, titleSizePx, trackingPx)
        val secondInk = secondLayout.paintInk(secondLine, extraBold, titleSizePx, trackingPx)
        val inkShift = -secondInk.top
        val oIndex = secondLine.lastIndexOf('o')
        val rIndex = secondLine.lastIndexOf('r')
        val eIndex = secondLine.lastIndexOf('e')
        val oBox = if (oIndex >= 0) {
            secondLayout.getBoundingBox(oIndex).translate(0f, inkShift)
        } else {
            Rect.Zero
        }
        val rBox = if (rIndex >= 0) {
            secondLayout.getBoundingBox(rIndex).translate(0f, inkShift)
        } else {
            Rect.Zero
        }
        val eBox = if (eIndex >= 0) {
            secondLayout.getBoundingBox(eIndex).translate(0f, inkShift)
        } else {
            rBox
        }
        val cameraHeightPx = oBox.height * 0.50f
        val cameraWidthPx = cameraHeightPx * 360f / 330f
        val cameraLeftPx = (rBox.left + eBox.right) / 2f - cameraWidthPx / 2f
        val cameraTopPx = secondInk.height + with(density) { 1.dp.toPx() }
        val leftLug = Offset(
            x = cameraLeftPx + cameraWidthPx * 0.14f,
            y = cameraTopPx + cameraHeightPx * 0.18f,
        )
        val rightLug = Offset(
            x = cameraLeftPx + cameraWidthPx * 0.86f,
            y = cameraTopPx + cameraHeightPx * 0.18f,
        )
        val cameraWidth = with(density) { cameraWidthPx.toDp() }
        val cameraHeight = with(density) { cameraHeightPx.toDp() }
        val cameraX = with(density) { cameraLeftPx.toDp() }
        val cameraY = with(density) { cameraTopPx.toDp() }
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(48.dp))
            CroppedHeadlineLine(
                layout = kickerLayout,
                ink = kickerInk,
                color = kickerColor,
            )
            Spacer(modifier = Modifier.height(10.dp))
            CroppedHeadlineLine(
                layout = firstLayout,
                ink = firstInk,
                color = color,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = cameraHeight + 2.dp),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    if (oIndex < 0 || rIndex < 0) {
                        return@Canvas
                    }
                    drawPath(
                        path = cameraNeckStrapPath(
                            oBox = oBox,
                            rBox = rBox,
                            leftLug = leftLug,
                            rightLug = rightLug,
                        ),
                        color = strapColor,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
                CroppedHeadlineLine(
                    layout = secondLayout,
                    ink = secondInk,
                    color = color,
                )
                Image(
                    painter = painterResource(R.drawable.onboarding_camera),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = cameraX, y = cameraY)
                        .width(cameraWidth)
                        .height(cameraHeight),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

private fun widthFitHeadlineSize(
    measurer: TextMeasurer,
    text: String,
    maxPx: Int,
): Float {
    var low = 40f
    var high = 56f
    var best = 40f
    repeat(12) {
        val mid = (low + high) / 2f
        val layout = measurer.measure(
            text = text,
            style = OnboardingTightText.merge(
                TextStyle(
                    fontFamily = OnboardingDisplayFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = mid.sp,
                    letterSpacing = (-1.2).sp,
                    lineHeight = mid.sp,
                ),
            ),
            maxLines = 1,
            softWrap = false,
        )
        if (layout.size.width <= maxPx) {
            best = mid
            low = mid
        } else {
            high = mid
        }
    }
    return best
}

private fun TextLayoutResult.paintInk(
    text: String,
    typeface: Typeface?,
    textSizePx: Float,
    trackingPx: Float,
): Rect {
    if (text.isEmpty() || typeface == null || textSizePx <= 0f) {
        val bounds = getPathForRange(0, text.length).getBounds()
        return if (bounds.isEmpty) {
            Rect(0f, 0f, size.width.toFloat(), size.height.toFloat())
        } else {
            bounds
        }
    }
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        letterSpacing = trackingPx / textSizePx
    }
    val native = android.graphics.Rect()
    paint.getTextBounds(text, 0, text.length, native)
    val baseline = getLineBaseline(0)
    return Rect(
        left = native.left.toFloat(),
        top = baseline + native.top,
        right = native.right.toFloat(),
        bottom = baseline + native.bottom,
    )
}

@Composable
private fun CroppedHeadlineLine(
    layout: TextLayoutResult,
    ink: Rect,
    color: Color,
) {
    val density = LocalDensity.current
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(density) { ink.height.toDp() }),
    ) {
        drawText(
            textLayoutResult = layout,
            color = color,
            topLeft = Offset(0f, -ink.top),
        )
    }
}

private fun cameraNeckStrapPath(
    oBox: Rect,
    rBox: Rect,
    leftLug: Offset,
    rightLug: Offset,
): Path {
    val overO = Offset(oBox.center.x, oBox.top + oBox.height * 0.12f)
    val overR = Offset(rBox.center.x, rBox.top + rBox.height * 0.14f)
    return Path().apply {
        moveTo(leftLug.x, leftLug.y)
        cubicTo(
            leftLug.x,
            oBox.center.y,
            oBox.left,
            overO.y,
            overO.x,
            overO.y,
        )
        cubicTo(
            overR.x,
            overR.y,
            rightLug.x,
            rBox.center.y,
            rightLug.x,
            rightLug.y,
        )
    }
}

@Composable
private fun OnboardingPageDots(
    pageCount: Int,
    selectedColor: Color,
    unselectedColor: Color,
    pageDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics { contentDescription = pageDescription },
        horizontalArrangement = Arrangement.Start,
    ) {
        repeat(pageCount) { index ->
            val selected = index == 0
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .height(8.dp)
                    .width(if (selected) 28.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (selected) selectedColor else unselectedColor),
            )
        }
    }
}

/**
 * Root composable that binds [OnboardingViewModel] and handles navigation.
 *
 * @param onNavigateNext Called when onboarding completes.
 * @param navController Navigation controller for scoped ViewModel ownership.
 * @author udit
 */
@Composable
fun OnboardingScreenRoot(
    onNavigateNext: () -> Unit,
    navController: NavHostController,
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val viewModel: OnboardingViewModel = navBackStackEntry?.let {
        koinViewModel(viewModelStoreOwner = it)
    } ?: koinViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateNext by viewModel.navigateNext.collectAsStateWithLifecycle()

    LaunchedEffect(navigateNext) {
        if (navigateNext) {
            onNavigateNext()
            viewModel.resetNavigation()
        }
    }

    OnboardingScreenContent(
        state = uiState,
        onAction = viewModel::handleAction,
    )
}

@Preview(name = "Loaded", showBackground = true)
@Composable
private fun OnboardingScreenContentLoadedPreview() {
    MaterialTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(),
            onAction = {},
        )
    }
}
