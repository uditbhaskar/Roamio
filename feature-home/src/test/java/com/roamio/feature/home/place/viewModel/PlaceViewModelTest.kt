package com.roamio.feature.home.place.viewModel

import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.result.AppResult
import com.roamio.feature.home.place.data.PlaceRepository
import com.roamio.feature.home.place.data.PlaceSnapshot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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
 * Unit tests for [PlaceViewModel] load and back.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaceViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: PlaceRepository = mockk(relaxed = true)
    private lateinit var viewModel: PlaceViewModel

    /**
     * Configures the main dispatcher and successful load stub.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeSaved() } returns flowOf(emptyList())
        coEvery { repository.load() } returns AppResult.Success(
            PlaceSnapshot(
                place = ExplorePlace(
                    osmId = 1,
                    osmType = "node",
                    name = "Fløyen",
                    latitude = 60.39,
                    longitude = 5.33,
                    activity = ActivityKind.HIKING,
                ),
                weather = null,
                nearby = null,
                isSaved = false,
            ),
        )
        viewModel = PlaceViewModel(repository)
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
     * Verifies the place name is exposed after load.
     *
     * @author udit
     */
    @Test
    fun load_success_setsName() = runTest {
        assertEquals("Fløyen", viewModel.uiState.value.place?.name)
    }

    /**
     * Verifies Back requests navigation.
     *
     * @author udit
     */
    @Test
    fun back_setsNavigateBack() = runTest {
        viewModel.handleAction(PlaceAction.Back)
        assertTrue(viewModel.navigateBack.value)
    }
}
