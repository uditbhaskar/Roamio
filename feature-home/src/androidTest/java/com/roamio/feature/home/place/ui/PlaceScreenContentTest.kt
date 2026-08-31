package com.roamio.feature.home.place.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.feature.home.place.viewModel.PlaceUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [PlaceScreenContent] title visibility.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class PlaceScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies the place name is shown when loaded.
     *
     * @author udit
     */
    @Test
    fun placeName_isDisplayed() {
        composeTestRule.setContent {
            PlaceScreenContent(
                state = PlaceUiState(
                    isLoading = false,
                    place = ExplorePlace(
                        osmId = 1,
                        osmType = "node",
                        name = "Tyrolean Alps",
                        latitude = 47.2,
                        longitude = 11.4,
                        activity = ActivityKind.HIKING,
                        countryCode = "AT",
                    ),
                ),
                onAction = {},
            )
        }
        composeTestRule.onNodeWithText("Tyrolean Alps").assertExists()
    }
}
