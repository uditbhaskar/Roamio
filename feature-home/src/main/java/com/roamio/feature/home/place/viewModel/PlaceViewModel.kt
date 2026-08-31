package com.roamio.feature.home.place.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.constants.CoreConstants
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.NearbyBite
import com.roamio.core.places.SavedPlaceRecord
import com.roamio.core.result.AppResult
import com.roamio.core.util.GeoUtils
import com.roamio.core.weather.CurrentWeather
import com.roamio.feature.home.place.data.PlaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for the place detail screen.
 *
 * @param placeRepository Detail loader and save toggle.
 * @author udit
 */
class PlaceViewModel(
    private val placeRepository: PlaceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaceUiState())
    val uiState: StateFlow<PlaceUiState> = _uiState
    private val _navigateBack = MutableStateFlow(false)
    val navigateBack: StateFlow<Boolean> = _navigateBack
    private var loadJob: Job? = null
    private var savedRecords: List<SavedPlaceRecord> = emptyList()

    init {
        placeRepository.observeSaved()
            .onEach { records ->
                savedRecords = records
                syncStar()
            }
            .launchIn(viewModelScope)
        refresh()
    }

    /**
     * Handles detail user actions.
     *
     * @param action User action from the screen.
     * @author udit
     */
    fun handleAction(action: PlaceAction) {
        when (action) {
            PlaceAction.Open -> refresh()
            PlaceAction.Retry -> refresh()
            PlaceAction.Back -> _navigateBack.value = true
            PlaceAction.ToggleSave -> toggleSave()
        }
    }

    /**
     * Clears the back-navigation flag.
     *
     * @author udit
     */
    fun resetNavigation() {
        _navigateBack.value = false
    }

    private fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val seed = placeRepository.current()
            _uiState.value = _uiState.value.copy(
                isLoading = seed == null,
                isEnriching = seed != null,
                errorMessage = null,
                place = seed,
                detailFocus = null,
                focusUnavailable = false,
            )
            when (val result = placeRepository.load()) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEnriching = false,
                        errorMessage = null,
                        place = data.place,
                        weather = data.weather,
                        nearby = data.nearby,
                        detailFocus = data.detailFocus,
                        focusUnavailable = data.focusUnavailable,
                    )
                    syncStar()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEnriching = false,
                        errorMessage = result.message.ifBlank { CoreConstants.Errors.UNKNOWN },
                    )
                }
            }
        }
    }

    private fun toggleSave() {
        val place = _uiState.value.place ?: return
        _uiState.value = _uiState.value.copy(isSaved = !_uiState.value.isSaved)
        viewModelScope.launch {
            placeRepository.toggleSaved(place)
        }
    }

    private fun syncStar() {
        val place = _uiState.value.place ?: return
        val saved = isStarred(place)
        if (saved != _uiState.value.isSaved) {
            _uiState.value = _uiState.value.copy(isSaved = saved)
        }
    }

    private fun isStarred(place: ExplorePlace): Boolean {
        return savedRecords.any { GeoUtils.sameSavedPlace(place, it) }
    }
}

/**
 * Render state for place detail.
 *
 * @param isLoading True while detail content loads.
 * @param isEnriching True while Wikipedia and extras replace the seed snapshot.
 * @param errorMessage User-facing error, when any.
 * @param place Selected place.
 * @param weather Current weather.
 * @param nearby Nearest café card when the place is not café-first.
 * @param isSaved Star state.
 * @param detailFocus Chip section that could not resolve live data.
 * @param focusUnavailable True when [detailFocus] had no nearby match.
 * @author udit
 */
data class PlaceUiState(
    val isLoading: Boolean = true,
    val isEnriching: Boolean = false,
    val errorMessage: String? = null,
    val place: ExplorePlace? = null,
    val weather: CurrentWeather? = null,
    val nearby: NearbyBite? = null,
    val isSaved: Boolean = false,
    val detailFocus: ActivityKind? = null,
    val focusUnavailable: Boolean = false,
)

/**
 * User actions on place detail.
 *
 * @author udit
 */
sealed interface PlaceAction {
    data object Open : PlaceAction
    data object Retry : PlaceAction
    data object Back : PlaceAction
    data object ToggleSave : PlaceAction
}
