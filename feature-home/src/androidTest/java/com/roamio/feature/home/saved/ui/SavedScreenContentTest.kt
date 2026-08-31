package com.roamio.feature.home.saved.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.feature.home.saved.viewModel.SavedUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [SavedScreenContent] empty copy.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class SavedScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies the empty state string is shown.
     *
     * @author udit
     */
    @Test
    fun empty_showsMessage() {
        composeTestRule.setContent {
            SavedScreenContent(state = SavedUiState(), onAction = {})
        }
        composeTestRule.onNodeWithText("Star a place to see it here.").assertExists()
    }
}
