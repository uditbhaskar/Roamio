package com.roamio.feature.home.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.constants.CoreConstants
import com.roamio.core.preferences.AppPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val NAME_SAVE_DEBOUNCE_MS = 450L
private const val SAVED_HINT_VISIBLE_MS = 2_000L

/**
 * ViewModel for traveler settings.
 *
 * @param appPreferences Persistent name, units, and currency.
 * @author udit
 */
class SettingsViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val nameDraft = MutableStateFlow<String?>(null)
    private val showSavedHint = MutableStateFlow(false)
    private var nameSaveJob: Job? = null
    private var savedHintJob: Job? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        appPreferences.displayName,
        appPreferences.useCelsius,
        appPreferences.homeCurrency,
        nameDraft,
        showSavedHint,
    ) { name, celsius, currency, draft, saved ->
        SettingsUiState(
            displayName = draft ?: name,
            useCelsius = celsius,
            homeCurrency = currency,
            showSavedHint = saved,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SettingsUiState(),
    )

    /**
     * Handles settings user actions.
     *
     * @param action User action from the screen.
     * @author udit
     */
    fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.NameChanged -> onNameChanged(action.value)
            is SettingsAction.UseCelsius -> persistImmediately { appPreferences.setUseCelsius(action.value) }
            is SettingsAction.CurrencyChanged -> persistImmediately {
                appPreferences.setHomeCurrency(action.code)
            }
        }
    }

    private fun onNameChanged(value: String) {
        nameDraft.value = value
        nameSaveJob?.cancel()
        nameSaveJob = viewModelScope.launch {
            delay(NAME_SAVE_DEBOUNCE_MS)
            appPreferences.setDisplayName(value)
            nameDraft.value = null
            flashSavedHint()
        }
    }

    private fun persistImmediately(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
            flashSavedHint()
        }
    }

    private fun flashSavedHint() {
        savedHintJob?.cancel()
        savedHintJob = viewModelScope.launch {
            showSavedHint.value = true
            delay(SAVED_HINT_VISIBLE_MS)
            showSavedHint.value = false
        }
    }
}

/**
 * Render state for Settings.
 *
 * @param displayName Greeting name.
 * @param useCelsius Temperature unit flag.
 * @param homeCurrency Default convert-from code.
 * @param showSavedHint True while the saved confirmation is visible.
 * @author udit
 */
data class SettingsUiState(
    val displayName: String = "",
    val useCelsius: Boolean = true,
    val homeCurrency: String = CoreConstants.Preferences.DEFAULT_HOME_CURRENCY,
    val showSavedHint: Boolean = false,
)

/**
 * User actions on Settings.
 *
 * @author udit
 */
sealed interface SettingsAction {
    data class NameChanged(val value: String) : SettingsAction
    data class UseCelsius(val value: Boolean) : SettingsAction
    data class CurrencyChanged(val code: String) : SettingsAction
}
