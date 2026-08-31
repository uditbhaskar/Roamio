package com.roamio.feature.home.viewModel

import com.roamio.core.location.GeoLocation
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.ExploreSession
import com.roamio.core.places.SearchHit
import com.roamio.core.places.SelectedPlaceStore
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.result.AppResult
import com.roamio.feature.home.data.HomeRepository
import com.roamio.feature.home.data.HomeSnapshot
import com.roamio.feature.home.data.HomeWarmup
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [HomeViewModel] loading and Start Trip.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: HomeRepository = mockk(relaxed = true)
    private val preferences: AppPreferences = mockk(relaxed = true)
    private val store = SelectedPlaceStore()
    private lateinit var viewModel: HomeViewModel

    private val londonPlace = ExplorePlace(
        osmId = 1,
        osmType = "node",
        name = "London",
        latitude = 51.5,
        longitude = -0.12,
        activity = ActivityKind.PLACE,
        countryCode = "GB",
    )

    private val hikingPlace = ExplorePlace(
        osmId = 2,
        osmType = "node",
        name = "Hampstead Heath",
        latitude = 51.56,
        longitude = -0.16,
        activity = ActivityKind.HIKING,
    )

    /**
     * Configures the main dispatcher and successful load stub.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        io.mockk.every { preferences.displayName } returns MutableStateFlow("Morgan")
        io.mockk.every { preferences.useCelsius } returns MutableStateFlow(true)
        io.mockk.every { preferences.homeCurrency } returns MutableStateFlow("USD")
        coEvery { repository.load(any()) } returns AppResult.Success(
            HomeSnapshot(
                location = GeoLocation(51.5, -0.12, placeName = "London", countryCode = "GB"),
                weather = null,
                cityPlace = londonPlace,
                activityPlaces = mapOf(ActivityKind.HIKING to hikingPlace),
                loadedActivities = setOf(ActivityKind.HIKING),
                displayName = "Morgan",
                useCelsius = true,
            ),
        )
        viewModel = HomeViewModel(repository, mockk(relaxed = true), store, ExploreSession(), preferences)
    }

    /**
     * Resets the main dispatcher after each test.
     *
     * @author udit
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun finishInitialLoad() {
        viewModel.handleAction(HomeAction.LocationDenied)
    }

    /**
     * Verifies Home starts on a loader instead of a fallback city card.
     *
     * @author udit
     */
    @Test
    fun initialState_showsLoaderWithoutFallbackCity() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.featured)
        assertEquals("", viewModel.uiState.value.headlineCity)
    }

    /**
     * Verifies a successful load exposes the city overview by default.
     *
     * @author udit
     */
    @Test
    fun load_success_setsCityOverview() = runTest {
        finishInitialLoad()
        assertEquals("London", viewModel.uiState.value.featured?.name)
        assertEquals(ActivityKind.PLACE, viewModel.uiState.value.selectedActivity)
        assertEquals("Morgan", viewModel.uiState.value.greetingName)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    /**
     * Verifies a search hit updates the Home headline before extras finish.
     *
     * @author udit
     */
    @Test
    fun selectSearch_setsHeadlineImmediately() = runTest {
        finishInitialLoad()
        val kyotoPlace = ExplorePlace(
            osmId = 3,
            osmType = "node",
            name = "Kyoto",
            latitude = 35.0,
            longitude = 135.0,
            activity = ActivityKind.PLACE,
            countryCode = "JP",
        )
        coEvery { repository.load(any()) } returns AppResult.Success(
            HomeSnapshot(
                location = GeoLocation(35.0, 135.0, placeName = "Kyoto, Japan", countryCode = "JP"),
                weather = null,
                cityPlace = kyotoPlace,
                displayName = "Morgan",
                useCelsius = true,
            ),
        )
        viewModel.handleAction(
            HomeAction.SelectSearch(SearchHit(name = "Kyoto", latitude = 35.0, longitude = 135.0, countryCode = "JP")),
        )
        assertEquals("Kyoto", viewModel.uiState.value.headlineCity)
        assertEquals("JP", viewModel.uiState.value.countryCode)
        assertEquals("Kyoto", viewModel.uiState.value.featured?.name)
        assertEquals(ActivityKind.PLACE, viewModel.uiState.value.selectedActivity)
        assertFalse(viewModel.uiState.value.isSearchOpen)
    }

    /**
     * Verifies an activity chip swaps to the cached activity place.
     *
     * @author udit
     */
    @Test
    fun selectActivity_swapsFeaturedPlace() = runTest {
        finishInitialLoad()
        viewModel.handleAction(HomeAction.SelectActivity(ActivityKind.HIKING))
        assertEquals(ActivityKind.HIKING, viewModel.uiState.value.selectedActivity)
        assertEquals("Hampstead Heath", viewModel.uiState.value.featured?.name)
    }

    /**
     * Verifies tapping the active chip returns to the city overview.
     *
     * @author udit
     */
    @Test
    fun selectActivity_togglesBackToPlaceOverview() = runTest {
        finishInitialLoad()
        viewModel.handleAction(HomeAction.SelectActivity(ActivityKind.HIKING))
        viewModel.handleAction(HomeAction.SelectActivity(ActivityKind.PLACE))
        assertEquals(ActivityKind.PLACE, viewModel.uiState.value.selectedActivity)
        assertEquals("London", viewModel.uiState.value.featured?.name)
    }

    /**
     * Verifies a new search city resets to the place overview.
     *
     * @author udit
     */
    @Test
    fun selectSearch_resetsToPlaceOverview() = runTest {
        finishInitialLoad()
        viewModel.handleAction(HomeAction.SelectActivity(ActivityKind.HIKING))
        assertEquals(ActivityKind.HIKING, viewModel.uiState.value.selectedActivity)
        val kyotoPlace = ExplorePlace(
            osmId = 4,
            osmType = "node",
            name = "Kyoto",
            latitude = 35.0,
            longitude = 135.0,
            activity = ActivityKind.PLACE,
            countryCode = "JP",
        )
        coEvery { repository.load(any()) } returns AppResult.Success(
            HomeSnapshot(
                location = GeoLocation(35.0, 135.0, placeName = "Kyoto, Japan", countryCode = "JP"),
                weather = null,
                cityPlace = kyotoPlace,
                displayName = "Morgan",
                useCelsius = true,
            ),
        )
        viewModel.handleAction(
            HomeAction.SelectSearch(SearchHit(name = "Kyoto", latitude = 35.0, longitude = 135.0, countryCode = "JP")),
        )
        assertEquals(ActivityKind.PLACE, viewModel.uiState.value.selectedActivity)
        assertEquals("Kyoto", viewModel.uiState.value.headlineCity)
    }

    /**
     * Verifies returning to Home after a chip swap does not hit the repository again.
     *
     * @author udit
     */
    @Test
    fun becameVisible_afterChipChange_doesNotReload() = runTest {
        finishInitialLoad()
        viewModel.handleAction(HomeAction.SelectActivity(ActivityKind.HIKING))
        viewModel.handleAction(HomeAction.BecameVisible)
        io.mockk.coVerify(exactly = 1) { repository.load(any()) }
        assertEquals("Hampstead Heath", viewModel.uiState.value.featured?.name)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    /**
     * Verifies a Popular city keeps the card photo visible while Home reloads.
     *
     * @author udit
     */
    @Test
    fun becameVisible_fromPopular_showsPlaceholderHero() = runTest {
        coEvery { repository.load(any()) } coAnswers { kotlinx.coroutines.awaitCancellation() }
        val warmup = mockk<HomeWarmup>(relaxed = true)
        io.mockk.every { warmup.peekSnapshot() } returns null
        val session = ExploreSession()
        session.setOverride(
            location = GeoLocation(
                latitude = 60.39,
                longitude = 5.32,
                placeName = "Bergen",
                countryCode = "NO",
            ),
            photoUrl = "https://example.com/bergen.jpg",
        )
        viewModel = HomeViewModel(repository, warmup, store, session, preferences)
        viewModel.handleAction(HomeAction.BecameVisible)
        val state = viewModel.uiState.value
        assertEquals("Bergen", state.headlineCity)
        assertFalse(state.isLoading)
        assertEquals("https://example.com/bergen.jpg", state.featured?.photoUrl)
    }

    /**
     * Verifies a successful load never surfaces a hero error.
     *
     * @author udit
     */
    @Test
    fun load_success_neverSetsHeroError() = runTest {
        finishInitialLoad()
        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertEquals("London", viewModel.uiState.value.featured?.name)
    }

    /**
     * Verifies Cafe opens place detail without swapping the Home hero.
     *
     * @author udit
     */
    @Test
    fun selectCafe_opensCafeDetailWithoutHeroSwap() = runTest {
        val cafePlace = ExplorePlace(
            osmId = 3,
            osmType = "node",
            name = "Monmouth Coffee",
            latitude = 51.51,
            longitude = -0.12,
            activity = ActivityKind.CAFE,
            blurb = "Specialty coffee · Covent Garden",
            countryCode = "GB",
            city = "London",
        )
        coEvery { repository.load(any()) } returns AppResult.Success(
            HomeSnapshot(
                location = GeoLocation(51.5, -0.12, placeName = "London", countryCode = "GB"),
                weather = null,
                cityPlace = londonPlace,
                activityPlaces = mapOf(ActivityKind.HIKING to hikingPlace),
                cafePlace = cafePlace,
                loadedActivities = setOf(ActivityKind.HIKING, ActivityKind.CAFE),
                displayName = "Morgan",
                useCelsius = true,
            ),
        )
        viewModel = HomeViewModel(repository, mockk(relaxed = true), store, ExploreSession(), preferences)
        finishInitialLoad()
        viewModel.handleAction(HomeAction.SelectActivity(ActivityKind.CAFE))
        assertEquals(ActivityKind.PLACE, viewModel.uiState.value.selectedActivity)
        assertEquals("London", viewModel.uiState.value.featured?.name)
        assertTrue(viewModel.navigateToPlace.value)
        assertFalse(store.includeNearbyCafe)
        assertEquals(ActivityKind.CAFE, store.place?.activity)
        assertEquals("Monmouth Coffee", store.place?.name)
    }

    /**
     * Verifies Start Trip stores the place and requests navigation.
     *
     * @author udit
     */
    @Test
    fun startTrip_setsNavigation() = runTest {
        finishInitialLoad()
        viewModel.handleAction(HomeAction.StartTrip)
        assertTrue(viewModel.navigateToPlace.value)
        assertFalse(store.includeNearbyCafe)
        assertEquals("London", store.place?.name)
    }
}
