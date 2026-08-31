package com.roamio.ui.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.roamio.R
import com.roamio.core.R as CoreR
import com.roamio.feature.home.currency.ui.CurrencyScreenRoot
import com.roamio.feature.home.popular.ui.PopularScreenRoot
import com.roamio.feature.home.saved.ui.SavedScreenRoot
import com.roamio.feature.home.settings.ui.SettingsScreenRoot
import com.roamio.feature.home.ui.HomeScreenRoot
import com.roamio.feature.home.ui.motion.RoamioMotion

private val DockShape = RoundedCornerShape(32.dp)

/**
 * Main tab host with the floating glass dock from the mock.
 *
 * @param onOpenPlace Opens the place detail route.
 * @author udit
 */
@Composable
fun ExploreShell(
    onOpenPlace: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = tab,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                RoamioMotion.tabSlide(forward = targetState > initialState)
            },
            label = "explore_tab",
        ) { selected ->
            when (selected) {
                0 -> HomeScreenRoot(onOpenPlace = onOpenPlace)
                1 -> PopularScreenRoot(
                    onOpenHome = { tab = 0 },
                    onOpenSaved = { tab = 2 },
                    onOpenSettings = { tab = 4 },
                )
                2 -> SavedScreenRoot(onOpenPlace = onOpenPlace)
                3 -> CurrencyScreenRoot()
                else -> SettingsScreenRoot()
            }
        }
        GlassDock(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Frosted iOS-style dock that switches the five explore tabs.
 *
 * @param selected Index of the active tab.
 * @param onSelect Called with the tapped tab index.
 * @param modifier Layout modifier from the shell.
 * @author udit
 */
@Composable
private fun GlassDock(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val muted = colorResource(CoreR.color.roamio_muted)
    val glassFill = Color.White.copy(alpha = 0.78f)
    val glassBorder = Color.White.copy(alpha = 0.52f)
    val glassHighlight = forest.copy(alpha = 0.1f)
    val items = listOf(
        DockItem(Icons.Filled.Home, stringResource(R.string.dock_home)),
        DockItem(Icons.Filled.GridView, stringResource(R.string.dock_popular)),
        DockItem(Icons.Filled.Bookmark, stringResource(R.string.dock_saved)),
        DockItem(Icons.Filled.CurrencyExchange, stringResource(R.string.dock_convert)),
        DockItem(Icons.Filled.Person, stringResource(R.string.dock_settings)),
    )
    BoxWithConstraints(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, bottom = 12.dp)
            .fillMaxWidth()
            .height(64.dp),
    ) {
        val slotWidth = maxWidth / items.size
        val indicatorOffset by animateDpAsState(
            targetValue = slotWidth * selected,
            animationSpec = RoamioMotion.bouncySpringDp,
            label = "dock_indicator_offset",
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 18.dp,
                    shape = DockShape,
                    clip = false,
                    ambientColor = forest.copy(alpha = 0.14f),
                    spotColor = forest.copy(alpha = 0.2f),
                )
                .clip(DockShape)
                .background(glassFill)
                .border(width = 1.dp, color = glassBorder, shape = DockShape),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                    .width(slotWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 7.dp, vertical = 6.dp)
                    .clip(CircleShape)
                    .background(glassHighlight)
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.65f),
                        shape = CircleShape,
                    ),
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    DockTab(
                        item = item,
                        selected = index == selected,
                        activeTint = forest,
                        inactiveTint = muted,
                        onClick = { onSelect(index) },
                        modifier = Modifier.width(slotWidth),
                    )
                }
            }
        }
    }
}

@Composable
private fun DockTab(
    item: DockItem,
    selected: Boolean,
    activeTint: Color,
    inactiveTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTint by animateColorAsState(
        targetValue = when {
            selected -> activeTint
            else -> inactiveTint
        },
        animationSpec = RoamioMotion.chipTween,
        label = "dock_icon_tint",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.18f else 1f,
        animationSpec = RoamioMotion.bouncySpring,
        label = "dock_icon_scale",
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier
                .scale(iconScale)
                .size(22.dp),
        )
    }
}

private data class DockItem(
    val icon: ImageVector,
    val label: String,
)
