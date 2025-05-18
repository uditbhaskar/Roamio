package com.roamio.feature.onboarding.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing the onboarding screen state and actions.
 *
 * @author udit
 */
class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    private val _navigateNext = MutableStateFlow(false)
    val navigateNext: StateFlow<Boolean> = _navigateNext

    /**
     * Handles onboarding actions and manages navigation state.
     * @param action The user action to process.
     * @author udit
     */
    fun handleAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.CONTINUE -> {
                _navigateNext.value = true
            }
            OnboardingAction.PAGE_CHANGED -> {}
        }
    }

    /**
     * Resets the navigation trigger after navigation occurs.
     * @author udit
     */
    fun resetNavigation() {
        _navigateNext.value = false
    }
}

/**
 * UI state for the onboarding screen.
 *
 * @property welcomeMessage The welcome message to display.
 * @property currentPage The current page index in the onboarding flow.
 * @author udit
 */
data class OnboardingUiState(
    val welcomeMessage: String = "Welcome to Roamio!",
    val currentPage: Int = 0
)

/**
 * Enum representing user actions on the onboarding screen.
 * @author udit
 */
enum class OnboardingAction {
    CONTINUE,
    PAGE_CHANGED
}