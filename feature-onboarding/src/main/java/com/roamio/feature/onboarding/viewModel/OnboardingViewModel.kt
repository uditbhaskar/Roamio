package com.roamio.feature.onboarding.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing onboarding persistence and navigation.
 *
 * @param appPreferences Preferences store for onboarding completion.
 * @author udit
 */
class OnboardingViewModel(
    private val appPreferences: AppPreferences,
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
            OnboardingAction.OPEN_PRIVACY -> Unit
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
            appPreferences.setOnboardingCompleted(true)
            _navigateNext.value = true
        }
    }
}

/**
 * UI state for the onboarding screen.
 *
 * @author udit
 */
class OnboardingUiState

/**
 * User actions available on the onboarding screen.
 *
 * @author udit
 */
enum class OnboardingAction {
    CONTINUE,
    OPEN_PRIVACY,
}
