package com.roamio.feature.onboarding.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roamio.feature.onboarding.viewModel.OnboardingAction
import com.roamio.feature.onboarding.viewModel.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [OnboardingScreenContent] interactions.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies that tapping Get Started dispatches [OnboardingAction.CONTINUE].
     *
     * @author udit
     */
    @Test
    fun getStartedButton_triggersContinueAction() {
        var action: OnboardingAction? = null
        composeTestRule.setContent {
            OnboardingScreenContent(
                state = OnboardingUiState(),
                onAction = { action = it },
            )
        }
        composeTestRule.onNodeWithText("Get Started").performClick()
        assertEquals(OnboardingAction.CONTINUE, action)
    }
}
