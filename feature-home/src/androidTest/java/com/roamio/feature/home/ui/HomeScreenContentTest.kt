package com.roamio.feature.home.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.feature.home.viewModel.HomeAction
import com.roamio.feature.home.viewModel.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [HomeScreenContent] Start Trip.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies that tapping Start Trip dispatches [HomeAction.StartTrip].
     *
     * @author udit
     */
    @Test
    fun startTrip_dispatchesAction() {
        var action: HomeAction? = null
        composeTestRule.setContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    greetingName = "Morgan",
                    countryCode = "NO",
                    countryName = "Norway",
                    headlineCity = "Nature",
                    featured = ExplorePlace(
                        osmId = 1,
                        osmType = "node",
                        name = "Forest Trail",
                        latitude = 60.0,
                        longitude = 5.0,
                        activity = ActivityKind.HIKING,
                    ),
                ),
                onAction = { action = it },
            )
        }
        composeTestRule.onNodeWithText("Start Trip").performClick()
        assertEquals(HomeAction.StartTrip, action)
    }
}
