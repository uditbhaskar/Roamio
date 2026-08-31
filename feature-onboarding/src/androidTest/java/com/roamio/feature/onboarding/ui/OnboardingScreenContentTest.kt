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
     * Verifies that tapping Dive in dispatches [OnboardingAction.CONTINUE].
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
        composeTestRule.onNodeWithText("Dive in").performClick()
        assertEquals(OnboardingAction.CONTINUE, action)
    }

    /**
     * Verifies that tapping Privacy Policy dispatches [OnboardingAction.OPEN_PRIVACY].
     *
     * @author udit
     */
    @Test
    fun privacyLink_triggersOpenPrivacyAction() {
        var action: OnboardingAction? = null
        composeTestRule.setContent {
            OnboardingScreenContent(
                state = OnboardingUiState(),
                onAction = { action = it },
            )
        }
        composeTestRule.onNodeWithText("Privacy Policy").performClick()
        assertEquals(OnboardingAction.OPEN_PRIVACY, action)
    }
}
