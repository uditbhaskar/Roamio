package com.roamio.feature.home.saved.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.places.SavedPlaceRecord
import com.roamio.core.places.SelectedPlaceStore
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.preferences.toExplorePlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for the local saved-places list.
 *
 * @param appPreferences Saved place store.
 * @param selectedPlaceStore Detail target.
 * @author udit
 */
class SavedViewModel(
    private val appPreferences: AppPreferences,
    private val selectedPlaceStore: SelectedPlaceStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState
    private val _navigateToPlace = MutableStateFlow(false)
    val navigateToPlace: StateFlow<Boolean> = _navigateToPlace

    init {
        appPreferences.savedPlaces
            .onEach { places -> _uiState.value = SavedUiState(places = places) }
            .launchIn(viewModelScope)
    }

    /**
     * Handles Saved user actions.
     *
     * @param action User action from the screen.
     * @author udit
     */
    fun handleAction(action: SavedAction) {
        when (action) {
            is SavedAction.Open -> {
                selectedPlaceStore.includeNearbyCafe = false
                selectedPlaceStore.place = action.record.toExplorePlace()
                _navigateToPlace.value = true
            }
            is SavedAction.Delete -> {
                viewModelScope.launch {
                    appPreferences.removeSaved(action.record)
                }
            }
        }
    }

    /**
     * Clears the place-navigation flag.
     *
     * @author udit
     */
    fun resetNavigation() {
        _navigateToPlace.value = false
    }
}

/**
 * Render state for Saved.
 *
 * @param places Starred records.
 * @author udit
 */
data class SavedUiState(
    val places: List<SavedPlaceRecord> = emptyList(),
)

/**
 * User actions on Saved.
 *
 * @author udit
 */
sealed interface SavedAction {
    data class Open(val record: SavedPlaceRecord) : SavedAction
    data class Delete(val record: SavedPlaceRecord) : SavedAction
}
