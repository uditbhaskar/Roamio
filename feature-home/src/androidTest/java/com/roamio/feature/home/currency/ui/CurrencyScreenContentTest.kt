package com.roamio.feature.home.currency.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.feature.home.currency.viewModel.CurrencyUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [CurrencyScreenContent] title.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class CurrencyScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies the Convert title is shown.
     *
     * @author udit
     */
    @Test
    fun title_isDisplayed() {
        composeTestRule.setContent {
            CurrencyScreenContent(state = CurrencyUiState(), onAction = {})
        }
        composeTestRule.onNodeWithText("Convert").assertExists()
    }
}
