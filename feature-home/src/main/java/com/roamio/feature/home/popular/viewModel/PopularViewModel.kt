package com.roamio.feature.home.popular.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.places.SeedCity
import com.roamio.core.result.AppResult
import com.roamio.feature.home.popular.data.PopularCityCard
import com.roamio.feature.home.popular.data.PopularRepository
import com.roamio.feature.home.popular.data.filtered
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Popular destinations screen.
 *
 * @param popularRepository Catalog loader.
 * @author udit
 */
class PopularViewModel(
    private val popularRepository: PopularRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PopularUiState())
    val uiState: StateFlow<PopularUiState> = _uiState
    private val _openHome = MutableStateFlow(false)
    val openHome: StateFlow<Boolean> = _openHome
    private val _openSaved = MutableStateFlow(false)
    val openSaved: StateFlow<Boolean> = _openSaved
    private val _openSettings = MutableStateFlow(false)
    val openSettings: StateFlow<Boolean> = _openSettings

    init {
        refresh()
    }

    /**
     * Handles Popular user actions.
     *
     * @param action User action from the screen.
     * @author udit
     */
    fun handleAction(action: PopularAction) {
        when (action) {
            PopularAction.Retry -> refresh()
            PopularAction.OpenSaved -> _openSaved.value = true
            PopularAction.OpenSettings -> _openSettings.value = true
            is PopularAction.SelectFilter -> {
                _uiState.value = _uiState.value.copy(filter = action.filter, deckIndex = 0)
            }
            PopularAction.AdvanceDeck -> {
                val count = _uiState.value.visibleCities.size
                if (count > 1) {
                    _uiState.value = _uiState.value.copy(
                        deckIndex = (_uiState.value.deckIndex + 1).mod(count),
                    )
                }
            }
            is PopularAction.OpenCity -> {
                popularRepository.openCity(action.city, action.photoUrl)
            }
        }
    }

    /**
     * Clears navigation flags after the shell has switched tabs.
     *
     * @author udit
     */
    fun resetNavigation() {
        _openHome.value = false
        _openSaved.value = false
        _openSettings.value = false
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.cities.isEmpty(),
                errorMessage = null,
            )
            when (val result = popularRepository.load()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cities = result.data.cities,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (_uiState.value.cities.isEmpty()) result.message else null,
                    )
                }
            }
        }
    }
}

/**
 * Render state for Popular.
 *
 * @param isLoading True while cities load.
 * @param errorMessage User-facing error, when any.
 * @param filter Selected chip.
 * @param cities Loaded catalog.
 * @param deckIndex Front card in the Popular stack.
 * @author udit
 */
data class PopularUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val filter: PopularFilter = PopularFilter.NEARBY,
    val cities: List<PopularCityCard> = emptyList(),
    val deckIndex: Int = 0,
) {
    val visibleCities: List<PopularCityCard> get() = cities.filtered(filter)

    val deckCities: List<PopularCityCard>
        get() {
            val list = visibleCities
            if (list.isEmpty()) return emptyList()
            val shift = deckIndex.mod(list.size)
            return list.drop(shift) + list.take(shift)
        }
}

/**
 * Filter chips under the Popular title.
 *
 * @author udit
 */
enum class PopularFilter {
    NEARBY,
    CLEAR_SKIES,
    HIKING,
    KAYAKING,
    BIKING,
}

/**
 * User actions on Popular.
 *
 * @author udit
 */
sealed interface PopularAction {
    data object Retry : PopularAction
    data object OpenSaved : PopularAction
    data object OpenSettings : PopularAction
    data class SelectFilter(val filter: PopularFilter) : PopularAction
    data object AdvanceDeck : PopularAction
    data class OpenCity(
        val city: SeedCity,
        val filter: PopularFilter,
        val photoUrl: String?,
    ) : PopularAction
}
