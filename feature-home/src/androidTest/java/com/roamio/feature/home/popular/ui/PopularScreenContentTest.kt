package com.roamio.feature.home.popular.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.core.places.SeedCity
import com.roamio.feature.home.popular.data.PopularCityCard
import com.roamio.feature.home.popular.viewModel.PopularUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [PopularScreenContent] title visibility.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class PopularScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies the Popular title and a city name are shown.
     *
     * @author udit
     */
    @Test
    fun title_isDisplayed() {
        composeTestRule.setContent {
            PopularScreenContent(
                state = PopularUiState(
                    isLoading = false,
                    cities = listOf(
                        PopularCityCard(
                            city = SeedCity("Bergen", "NO", 60.0, 5.0),
                            temperatureC = 15.0,
                            weatherCode = 0,
                            photoUrl = "https://example.com/bergen.jpg",
                            distanceKm = 10.0,
                        ),
                    ),
                ),
                onAction = {},
            )
        }
        composeTestRule.onNodeWithText("Popular").assertExists()
        composeTestRule.onNodeWithText("Bergen").assertExists()
    }
}
