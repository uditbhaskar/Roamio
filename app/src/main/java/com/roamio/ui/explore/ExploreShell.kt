package com.roamio.ui.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.roamio.R
import com.roamio.core.R as CoreR
import com.roamio.feature.home.currency.ui.CurrencyScreenRoot
import com.roamio.feature.home.popular.ui.PopularScreenRoot
import com.roamio.feature.home.saved.ui.SavedScreenRoot
import com.roamio.feature.home.settings.ui.SettingsScreenRoot
import com.roamio.feature.home.ui.HomeScreenRoot

/**
 * Main tab host with the floating forest dock from the mock.
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
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
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
        ForestDock(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Floating forest-green dock that switches the five explore tabs.
 *
 * @param selected Index of the active tab.
 * @param onSelect Called with the tapped tab index.
 * @param modifier Layout modifier from the shell.
 * @author udit
 */
@Composable
private fun ForestDock(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val forest = colorResource(CoreR.color.roamio_forest)
    val mint = colorResource(CoreR.color.roamio_mint)
    val muted = colorResource(CoreR.color.roamio_muted)
    val items = listOf(
        DockItem(Icons.Filled.Home, stringResource(R.string.dock_home)),
        DockItem(Icons.Filled.GridView, stringResource(R.string.dock_popular)),
        DockItem(Icons.Filled.Bookmark, stringResource(R.string.dock_saved)),
        DockItem(Icons.Filled.CurrencyExchange, stringResource(R.string.dock_convert)),
        DockItem(Icons.Filled.Person, stringResource(R.string.dock_settings)),
    )
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, bottom = 12.dp)
            .fillMaxWidth()
            .height(64.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp), clip = false)
            .clip(RoundedCornerShape(32.dp))
            .background(forest)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (active) mint else muted,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private data class DockItem(
    val icon: ImageVector,
    val label: String,
)
