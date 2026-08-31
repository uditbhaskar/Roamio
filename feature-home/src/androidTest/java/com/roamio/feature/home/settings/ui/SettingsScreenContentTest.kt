package com.roamio.feature.home.settings.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.feature.home.settings.viewModel.SettingsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [SettingsScreenContent] title.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies the Settings title is shown.
     *
     * @author udit
     */
    @Test
    fun title_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreenContent(state = SettingsUiState(), onAction = {})
        }
        composeTestRule.onNodeWithText("Settings").assertExists()
    }
}
