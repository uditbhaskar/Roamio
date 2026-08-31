package com.roamio.feature.home.popular.viewModel

import com.roamio.core.location.GeoLocation
import com.roamio.core.places.SeedCity
import com.roamio.core.result.AppResult
import com.roamio.feature.home.popular.data.PopularCityCard
import com.roamio.feature.home.popular.data.PopularRepository
import com.roamio.feature.home.popular.data.PopularSnapshot
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PopularViewModel] load and city open.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PopularViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: PopularRepository = mockk(relaxed = true)
    private lateinit var viewModel: PopularViewModel
    private val bergen = SeedCity("Bergen", "NO", 60.39, 5.32)

    /**
     * Configures the main dispatcher and successful load stub.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.load() } returns AppResult.Success(
            PopularSnapshot(
                origin = GeoLocation(51.5, -0.12, placeName = "London", countryCode = "GB"),
                cities = listOf(
                    PopularCityCard(bergen, 15.0, 0, "https://example.com/bergen.jpg", 900.0),
                    PopularCityCard(SeedCity("Innsbruck", "AT", 47.2, 11.4), 10.0, 0, "https://example.com/innsbruck.jpg", 80.0),
                    PopularCityCard(SeedCity("Banff", "CA", 51.1, -115.5), 6.0, 0, "https://example.com/banff.jpg", 200.0),
                ),
            ),
        )
        viewModel = PopularViewModel(repository)
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

    /**
     * Verifies cities load into state.
     *
     * @author udit
     */
    @Test
    fun load_success_setsCity() = runTest {
        assertEquals("Bergen", viewModel.uiState.value.cities.first().city.name)
    }

    /**
     * Verifies opening a city asks the shell to return Home.
     *
     * @author udit
     */
    @Test
    fun openCity_requestsHome() = runTest {
        viewModel.handleAction(PopularAction.OpenCity(bergen, PopularFilter.NEARBY, null))
        verify { repository.openCity(bergen, null) }
        assertTrue(viewModel.openHome.value)
    }

    /**
     * Verifies advancing the stack rotates the front city.
     *
     * @author udit
     */
    @Test
    fun advanceDeck_rotatesFrontCity() = runTest {
        viewModel.handleAction(PopularAction.SelectFilter(PopularFilter.HIKING))
        assertEquals("Bergen", viewModel.uiState.value.deckCities.first().city.name)
        viewModel.handleAction(PopularAction.AdvanceDeck)
        assertEquals("Innsbruck", viewModel.uiState.value.deckCities.first().city.name)
    }
}
