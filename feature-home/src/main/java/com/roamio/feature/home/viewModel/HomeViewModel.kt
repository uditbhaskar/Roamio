package com.roamio.feature.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roamio.core.location.GeoLocation
import com.roamio.core.constants.CoreConstants
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.ExploreSession
import com.roamio.core.places.SearchHit
import com.roamio.core.places.SeedCities
import com.roamio.core.places.SelectedPlaceStore
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.result.AppResult
import com.roamio.core.util.GeoUtils
import com.roamio.feature.home.data.HomeRepository
import com.roamio.feature.home.data.HomeSnapshot
import com.roamio.feature.home.data.HomeWarmup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Home discovery screen.
 *
 * @param homeRepository Location, weather, and featured place loader.
 * @param homeWarmup Preloaded Home snapshot from onboarding.
 * @param selectedPlaceStore Holds the place opened by Start Trip.
 * @param exploreSession Shared activity and search point.
 * @param appPreferences Traveler name, units, and home currency.
 * @author udit
 */
class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val homeWarmup: HomeWarmup,
    private val selectedPlaceStore: SelectedPlaceStore,
    private val exploreSession: ExploreSession,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialHomeState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private val _navigateToPlace = MutableStateFlow(false)
    val navigateToPlace: StateFlow<Boolean> = _navigateToPlace
    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var appliedKey: String? = null
    private val featuredByActivity = mutableMapOf<ActivityKind, ExplorePlace>()
    private var cachedCity: String = ""
    private var hasPresentedContent = false
    private var showErrorAfterLoadFailure = false

    init {
        viewModelScope.launch {
            combine(
                appPreferences.displayName,
                appPreferences.useCelsius,
                appPreferences.homeCurrency,
            ) { name, celsius, currency ->
                Triple(name, celsius, currency)
            }.collect { (name, celsius, currency) ->
                _uiState.value = _uiState.value.copy(
                    greetingName = name,
                    useCelsius = celsius,
                    homeCurrency = currency,
                )
            }
        }
    }

    private fun initialHomeState(): HomeUiState {
        return HomeUiState(isLoading = true)
    }

    /**
     * Handles Home user actions.
     *
     * @param action User action from the screen.
     * @author udit
     */
    fun handleAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> refresh()
            HomeAction.Retry -> {
                showErrorAfterLoadFailure = true
                refresh()
            }
            HomeAction.BecameVisible -> applyInitialOrSession()
            HomeAction.LocationGranted,
            HomeAction.LocationDenied,
            -> loadIfEmpty()
            is HomeAction.SelectActivity -> selectActivityChip(action.activity)
            HomeAction.StartTrip -> openFeatured()
            HomeAction.OpenSearch -> _uiState.value = _uiState.value.copy(isSearchOpen = true)
            HomeAction.CloseSearch -> _uiState.value = _uiState.value.copy(
                isSearchOpen = false,
                searchQuery = "",
                searchResults = emptyList(),
                searchErrorMessage = null,
            )
            is HomeAction.QueryChanged -> onQuery(action.query)
            is HomeAction.SelectSearch -> applySearchHit(action.hit)
        }
    }

    /**
     * Clears the place-navigation flag after the app has navigated.
     *
     * @author udit
     */
    fun resetNavigation() {
        _navigateToPlace.value = false
    }

    private fun applySearchHit(hit: SearchHit) {
        val city = hit.name.substringBefore(',').trim().ifBlank { hit.name }
        val photoUrl = SeedCities.photoFor(city, ActivityKind.PLACE).ifBlank { null }
        homeRepository.applySearch(hit, photoUrl)
        seedCity(
            city = city,
            countryCode = hit.countryCode,
            countryName = GeoUtils.countryName(hit.countryCode),
        )
        refresh()
    }

    private fun applyInitialOrSession() {
        if (!hasPresentedContent) {
            homeWarmup.peekSnapshot()?.let { data ->
                applySnapshot(data, exploreSession.activity)
            }
        }
        applySessionIfNeeded()
    }

    private fun applySessionIfNeeded() {
        var needsReload = false
        val override = exploreSession.overrideLocation
        if (override != null) {
            val city = override.placeName.substringBefore(',').trim().ifBlank { override.placeName }
            val photoUrl = exploreSession.takePendingPhotoUrl()
            if (city.isNotBlank() && (city != _uiState.value.headlineCity || photoUrl != null)) {
                seedCity(
                    city = city,
                    countryCode = override.countryCode,
                    countryName = GeoUtils.countryName(override.countryCode)
                        .ifBlank { override.placeName.substringAfter(',', "") },
                    location = override,
                    photoUrl = photoUrl,
                )
                needsReload = true
            }
        }
        if (needsReload) {
            if (loadJob?.isActive != true) {
                refresh()
            }
            return
        }
        if (_uiState.value.featured == null && loadJob?.isActive != true) {
            refresh()
        }
    }

    private fun loadIfEmpty() {
        if (_uiState.value.featured == null && loadJob?.isActive != true) {
            refresh()
        }
    }

    private fun applySnapshot(data: HomeSnapshot, activity: ActivityKind) {
        val city = data.location.placeName.substringBefore(',').trim()
            .ifBlank { data.location.placeName }
        val selected = when {
            activity == ActivityKind.PLACE || activity == ActivityKind.CAFE -> ActivityKind.PLACE
            data.loadedActivities.contains(activity) -> activity
            else -> ActivityKind.PLACE
        }
        val incoming = data.featuredFor(selected)
        val featured = incoming?.let { mergeFeaturedPlace(_uiState.value.featured, it) }
            ?: _uiState.value.featured
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            errorMessage = null,
            greetingName = data.displayName,
            countryCode = data.location.countryCode,
            countryName = GeoUtils.countryName(data.location.countryCode)
                .ifBlank { data.location.placeName.substringAfter(',', "") },
            headlineCity = city,
            temperature = data.weather?.temperatureC,
            weatherCode = data.weather?.weatherCode,
            useCelsius = data.useCelsius,
            loadedActivities = data.loadedActivities,
            selectedActivity = selected,
            featured = featured,
        )
        featuredByActivity.clear()
        data.cityPlace?.let { featuredByActivity[ActivityKind.PLACE] = it }
        data.cafePlace?.let { featuredByActivity[ActivityKind.CAFE] = it }
        data.activityPlaces.forEach { (kind, place) ->
            featuredByActivity[kind] = place
        }
        if (city.isNotBlank()) {
            cachedCity = city
        }
        hasPresentedContent = true
        appliedKey = sessionKey()
    }

    private fun seedCity(
        city: String,
        countryCode: String,
        countryName: String,
        location: GeoLocation? = null,
        photoUrl: String? = null,
    ) {
        featuredByActivity.clear()
        cachedCity = city
        exploreSession.activity = ActivityKind.PLACE
        val placeholder = location?.let {
            ExplorePlace(
                osmId = GeoUtils.fallbackOsmId(city, it.latitude, it.longitude),
                osmType = "seed",
                name = city,
                latitude = it.latitude,
                longitude = it.longitude,
                activity = ActivityKind.PLACE,
                photoUrl = photoUrl?.takeIf { url -> url.isNotBlank() },
                countryCode = countryCode,
                countryName = countryName,
                city = city,
            )
        }
        _uiState.value = _uiState.value.copy(
            isSearchOpen = false,
            searchQuery = "",
            searchResults = emptyList(),
            isLoading = placeholder == null,
            isRefreshing = placeholder != null,
            errorMessage = null,
            selectedActivity = ActivityKind.PLACE,
            loadedActivities = emptySet(),
            headlineCity = city,
            countryCode = countryCode,
            countryName = countryName,
            temperature = null,
            weatherCode = null,
            featured = placeholder,
        )
    }

    private fun refresh() {
        loadJob?.cancel()
        val expectedKey = sessionKey()
        appliedKey = expectedKey
        loadJob = viewModelScope.launch {
            val activity = exploreSession.activity
            val awaitingHero = _uiState.value.featured == null
            _uiState.value = _uiState.value.copy(
                isLoading = awaitingHero,
                isRefreshing = !awaitingHero,
                errorMessage = null,
                selectedActivity = activity,
            )
            when (val result = homeRepository.load(activity)) {
                is AppResult.Success -> {
                    if (sessionKey() != expectedKey) return@launch
                    val data = result.data
                    val city = data.location.placeName.substringBefore(',').trim()
                        .ifBlank { data.location.placeName }
                    val overrideCity = exploreSession.overrideLocation?.placeName
                        ?.substringBefore(',')
                        ?.trim()
                        .orEmpty()
                    if (
                        overrideCity.isNotBlank() &&
                        city.isNotBlank() &&
                        !city.equals(overrideCity, ignoreCase = true)
                    ) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = _uiState.value.featured == null,
                            isRefreshing = false,
                        )
                        return@launch
                    }
                    val selected = when {
                        activity == ActivityKind.PLACE || activity == ActivityKind.CAFE -> ActivityKind.PLACE
                        data.loadedActivities.contains(activity) -> activity
                        else -> ActivityKind.PLACE
                    }
                    applySnapshot(data, selected)
                }
                is AppResult.Error -> {
                    if (sessionKey() != expectedKey) return@launch
                    _uiState.value = if (
                        _uiState.value.featured == null && showErrorAfterLoadFailure
                    ) {
                        _uiState.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message.ifBlank { CoreConstants.Errors.NETWORK },
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = true,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                    showErrorAfterLoadFailure = false
                }
            }
        }
    }

    private fun sessionKey(): String {
        val loc = exploreSession.overrideLocation
        return listOf(loc?.latitude, loc?.longitude).joinToString()
    }

    private fun onQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchErrorMessage = null,
            )
            return
        }
        searchJob = viewModelScope.launch {
            delay(350.milliseconds)
            when (val result = homeRepository.search(query)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    searchResults = result.data,
                    searchErrorMessage = null,
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    searchErrorMessage = result.message,
                )
            }
        }
    }

    private fun selectActivityChip(activity: ActivityKind) {
        if (activity != ActivityKind.PLACE && activity !in _uiState.value.loadedActivities) {
            return
        }
        when (activity) {
            ActivityKind.POPULAR -> Unit
            ActivityKind.CAFE -> openCafeDetail()
            ActivityKind.HIKING,
            ActivityKind.KAYAKING,
            ActivityKind.BIKING,
            -> {
                val cached = featuredByActivity[activity]
                if (cached != null) {
                    exploreSession.activity = activity
                    appliedKey = sessionKey()
                    _uiState.value = _uiState.value.copy(
                        selectedActivity = activity,
                        errorMessage = null,
                        featured = cached,
                    )
                } else {
                    openPlaceDetail(focus = activity, loadCafe = false)
                }
            }
            ActivityKind.PLACE -> {
                val overview = cityOverviewPlace() ?: return
                exploreSession.activity = ActivityKind.PLACE
                appliedKey = sessionKey()
                _uiState.value = _uiState.value.copy(
                    selectedActivity = ActivityKind.PLACE,
                    errorMessage = null,
                    featured = overview,
                )
            }
        }
    }

    private fun openCafeDetail() {
        val cafe = featuredByActivity[ActivityKind.CAFE]
        if (cafe != null) {
            selectedPlaceStore.detailFocus = null
            selectedPlaceStore.includeNearbyCafe = false
            selectedPlaceStore.seededNearbyCafe = null
            selectedPlaceStore.place = cafe
            _navigateToPlace.value = true
            return
        }
        openPlaceDetail(focus = ActivityKind.CAFE, loadCafe = true)
    }

    private fun cityOverviewPlace(): ExplorePlace? {
        return featuredByActivity[ActivityKind.PLACE]
            ?: _uiState.value.featured?.takeIf { it.activity == ActivityKind.PLACE }
    }

    private fun openPlaceDetail(focus: ActivityKind, loadCafe: Boolean) {
        val place = cityOverviewPlace() ?: return
        selectedPlaceStore.detailFocus = focus
        selectedPlaceStore.includeNearbyCafe = loadCafe
        selectedPlaceStore.seededNearbyCafe = if (loadCafe) {
            featuredByActivity[ActivityKind.CAFE]?.toNearbyBite()
        } else {
            null
        }
        selectedPlaceStore.place = place
        _navigateToPlace.value = true
    }

    private fun openFeatured() {
        val place = _uiState.value.featured ?: return
        selectedPlaceStore.detailFocus = null
        selectedPlaceStore.includeNearbyCafe = false
        selectedPlaceStore.seededNearbyCafe = null
        selectedPlaceStore.place = place
        _navigateToPlace.value = true
    }
}

/**
 * Render state for Home.
 *
 * @param isLoading True while the featured place is loading and no placeholder exists.
 * @param isRefreshing True while a background refresh runs with a placeholder visible.
 * @param errorMessage Hero error, when any.
 * @param greetingName Name after "Hi,".
 * @param countryCode ISO country code.
 * @param countryName Country label under the flag.
 * @param headlineCity City used as the first headline word.
 * @param temperature Celsius from `Open-Meteo`.
 * @param weatherCode `Open-Meteo` weather code.
 * @param useCelsius Unit flag.
 * @param homeCurrency Default currency from Settings.
 * @param selectedActivity Active chip, or the place itself.
 * @param loadedActivities Chips that resolved live data for the current city.
 * @param featured Circular hero place.
 * @param isSearchOpen Whether the search overlay is visible.
 * @param searchQuery Text in the search field.
 * @param searchResults Nominatim hits.
 * @param searchErrorMessage Search overlay error, separate from the hero.
 * @author udit
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val greetingName: String = "",
    val countryCode: String = "",
    val countryName: String = "",
    val headlineCity: String = "",
    val temperature: Double? = null,
    val weatherCode: Int? = null,
    val useCelsius: Boolean = true,
    val homeCurrency: String = CoreConstants.Preferences.DEFAULT_HOME_CURRENCY,
    val selectedActivity: ActivityKind = ActivityKind.PLACE,
    val loadedActivities: Set<ActivityKind> = emptySet(),
    val featured: ExplorePlace? = null,
    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchHit> = emptyList(),
    val searchErrorMessage: String? = null,
)

/**
 * User actions on Home.
 *
 * @author udit
 */
sealed interface HomeAction {
    data object Refresh : HomeAction
    data object Retry : HomeAction
    data object BecameVisible : HomeAction
    data object LocationGranted : HomeAction
    data object LocationDenied : HomeAction
    data class SelectActivity(val activity: ActivityKind) : HomeAction
    data object StartTrip : HomeAction
    data object OpenSearch : HomeAction
    data object CloseSearch : HomeAction
    data class QueryChanged(val query: String) : HomeAction
    data class SelectSearch(val hit: SearchHit) : HomeAction
}
