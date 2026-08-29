package com.roamio.feature.onboarding.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.preferences.OnboardingPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing onboarding persistence and navigation.
 *
 * @param onboardingPreferences Preferences store for onboarding completion.
 * @author udit
 */
class OnboardingViewModel(
    private val onboardingPreferences: OnboardingPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())

    val uiState: StateFlow<OnboardingUiState> = _uiState

    private val _navigateNext = MutableStateFlow(false)

    val navigateNext: StateFlow<Boolean> = _navigateNext

    /**
     * Handles onboarding user actions and updates state or navigation.
     *
     * @param action The user action to process.
     * @author udit
     */
    fun handleAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.CONTINUE -> completeOnboarding()
        }
    }

    /**
     * Clears the navigation trigger after navigation has been handled.
     *
     * @author udit
     */
    fun resetNavigation() {
        _navigateNext.value = false
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
            _navigateNext.value = true
        }
    }
}

/**
 * UI state for the onboarding screen.
 *
 * @author udit
 */
data class OnboardingUiState(
    val pageCount: Int = 3,
)

/**
 * User actions available on the onboarding screen.
 *
 * @author udit
 */
enum class OnboardingAction {
    CONTINUE,
}
