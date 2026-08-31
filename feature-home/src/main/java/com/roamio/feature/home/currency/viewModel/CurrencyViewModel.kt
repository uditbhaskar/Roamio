package com.roamio.feature.home.currency.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.constants.CoreConstants
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.result.AppResult
import com.roamio.feature.home.currency.data.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the currency convert screen.
 *
 * @param currencyRepository Frankfurter converter.
 * @param appPreferences Home currency default.
 * @author udit
 */
class CurrencyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState

    init {
        viewModelScope.launch {
            appPreferences.homeCurrency.collect { home ->
                _uiState.value = _uiState.value.copy(fromCode = home)
            }
        }
    }

    /**
     * Handles convert-screen user actions.
     *
     * @param action User action from the screen.
     * @author udit
     */
    fun handleAction(action: CurrencyAction) {
        when (action) {
            is CurrencyAction.AmountChanged -> {
                _uiState.value = _uiState.value.copy(amount = action.value)
            }
            is CurrencyAction.FromChanged -> {
                _uiState.value = _uiState.value.copy(fromCode = action.code)
            }
            is CurrencyAction.ToChanged -> {
                _uiState.value = _uiState.value.copy(toCode = action.code)
            }
            CurrencyAction.Convert -> convert()
            CurrencyAction.Retry -> convert()
        }
    }

    private fun convert() {
        viewModelScope.launch {
            val amount = _uiState.value.amount.toDoubleOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (
                val result = currencyRepository.convert(
                    amount,
                    _uiState.value.fromCode,
                    _uiState.value.toCode,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        result = result.data,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }
}

/**
 * Render state for Convert.
 *
 * @param amount Typed source amount.
 * @param fromCode Source currency.
 * @param toCode Destination currency.
 * @param result Converted value.
 * @param isLoading True while Frankfurter is called.
 * @param errorMessage User-facing error, when any.
 * @author udit
 */
data class CurrencyUiState(
    val amount: String = "1",
    val fromCode: String = CoreConstants.Preferences.DEFAULT_HOME_CURRENCY,
    val toCode: String = "EUR",
    val result: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * User actions on Convert.
 *
 * @author udit
 */
sealed interface CurrencyAction {
    data class AmountChanged(val value: String) : CurrencyAction
    data class FromChanged(val code: String) : CurrencyAction
    data class ToChanged(val code: String) : CurrencyAction
    data object Convert : CurrencyAction
    data object Retry : CurrencyAction
}
