package com.roamio.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val HOME_CHIP_TO_HERO_SPACING_DP = 48

internal fun homeHeroCircleSize(maxWidth: Dp, maxHeight: Dp): Dp {
    return minOf(maxWidth, maxHeight)
}

@Composable
internal fun HeroContentShimmer(
    modifier: Modifier = Modifier,
) {
    val shapeLine = RoundedCornerShape(8.dp)
    val shapePill = RoundedCornerShape(24.dp)
    Box(modifier = modifier.clip(CircleShape)) {
        ShimmerBox(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(184.dp)
                    .height(24.dp)
                    .clip(shapeLine),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(14.dp)
                    .clip(shapeLine),
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(14.dp)
                    .clip(shapeLine),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                ShimmerBox(
                    modifier = Modifier
                        .width(56.dp)
                        .height(12.dp)
                        .clip(shapeLine),
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(48.dp)
                        .height(12.dp)
                        .clip(shapeLine),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(38.dp)
                    .clip(shapePill),
            )
        }
    }
}

/**
 * Full-screen shimmer skeleton for Home while the first snapshot loads.
 *
 * @param modifier Column modifier for the skeleton block.
 * @author udit
 */
@Composable
fun HomeLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val shapePill = RoundedCornerShape(22.dp)
    val shapeChip = RoundedCornerShape(22.dp)
    val shapeLine = RoundedCornerShape(8.dp)
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(128.dp)
                    .height(18.dp)
                    .clip(shapeLine),
            )
            ShimmerBox(
                modifier = Modifier
                    .width(72.dp)
                    .height(34.dp)
                    .clip(shapePill),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    modifier = Modifier
                        .width(72.dp)
                        .height(12.dp)
                        .clip(shapeLine),
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(42.dp)
                        .clip(shapeLine),
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox(
                    modifier = Modifier
                        .width(132.dp)
                        .height(42.dp)
                        .clip(shapeLine),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(52.dp)
                    .height(118.dp)
                    .clip(RoundedCornerShape(28.dp)),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(5) {
                ShimmerBox(
                    modifier = Modifier
                        .width(92.dp)
                        .height(38.dp)
                        .clip(shapeChip),
                )
            }
        }
        Spacer(modifier = Modifier.height(HOME_CHIP_TO_HERO_SPACING_DP.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            HeroContentShimmer(
                modifier = Modifier.size(homeHeroCircleSize(maxWidth, maxHeight)),
            )
        }
    }
}

@Preview(name = "Home skeleton", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeLoadingSkeletonPreview() {
    MaterialTheme {
        HomeLoadingSkeleton(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "Hero shimmer", showBackground = true, widthDp = 320, heightDp = 320)
@Composable
private fun HeroContentShimmerPreview() {
    MaterialTheme {
        HeroContentShimmer(modifier = Modifier.size(320.dp))
    }
}
