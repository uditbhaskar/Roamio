package com.roamio.feature.home.place.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roamio.core.R as CoreR
import com.roamio.feature.home.ui.ShimmerBox

private val shapeLine = RoundedCornerShape(8.dp)
private val shapePill = RoundedCornerShape(18.dp)

@Composable
internal fun PlaceBlurbShimmer(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(shapeLine),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(14.dp)
                .clip(shapeLine),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(14.dp)
                .clip(shapeLine),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .height(14.dp)
                .clip(shapeLine),
        )
    }
}

@Composable
internal fun PlaceLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val chip = colorResource(CoreR.color.roamio_chip)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(sage),
    ) {
        val heroHeight = maxHeight * 0.48f
        val sheetOverlap = 48.dp
        Box(modifier = Modifier.fillMaxSize()) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .align(Alignment.TopCenter),
            )
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(heroHeight - sheetOverlap))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                            .background(chip)
                            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
                            .navigationBarsPadding(),
                    ) {
                        ShimmerBox(
                            modifier = Modifier
                                .width(96.dp)
                                .height(12.dp)
                                .clip(shapeLine),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .height(32.dp)
                                .clip(shapeLine),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PlaceBlurbShimmer()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            repeat(3) {
                                ShimmerBox(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(shapePill),
                                )
                            }
                        }
                    }
                    ShimmerBox(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-22).dp, y = (-28).dp)
                            .size(58.dp)
                            .clip(CircleShape),
                    )
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
            ShimmerBox(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )
            ShimmerBox(
                modifier = Modifier
                    .width(72.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(20.dp)),
            )
        }
    }
}

@Preview(name = "Place skeleton", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlaceLoadingSkeletonPreview() {
    MaterialTheme {
        PlaceLoadingSkeleton()
    }
}

@Preview(name = "Place blurb shimmer", showBackground = true, widthDp = 342)
@Composable
private fun PlaceBlurbShimmerPreview() {
    MaterialTheme {
        PlaceBlurbShimmer(modifier = Modifier.padding(24.dp))
    }
}
