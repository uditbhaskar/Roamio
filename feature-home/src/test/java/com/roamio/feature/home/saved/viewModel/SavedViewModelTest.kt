package com.roamio.feature.home.saved.viewModel

import com.roamio.core.places.SavedPlaceRecord
import com.roamio.core.places.SelectedPlaceStore
import com.roamio.core.preferences.AppPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
 * Unit tests for [SavedViewModel] list and open.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val preferences: AppPreferences = mockk(relaxed = true)
    private val store = SelectedPlaceStore()
    private lateinit var viewModel: SavedViewModel
    private val record = SavedPlaceRecord(
        osmId = 1,
        osmType = "node",
        name = "Fløyen",
        latitude = 60.39,
        longitude = 5.33,
        countryCode = "NO",
        activity = "HIKING",
    )

    /**
     * Configures the main dispatcher and saved-place flow.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { preferences.savedPlaces } returns MutableStateFlow(listOf(record))
        viewModel = SavedViewModel(preferences, store)
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
     * Verifies saved records appear in state.
     *
     * @author udit
     */
    @Test
    fun load_exposesSavedName() = runTest {
        assertEquals("Fløyen", viewModel.uiState.value.places.first().name)
    }

    /**
     * Verifies opening a record stores it for detail.
     *
     * @author udit
     */
    @Test
    fun open_setsNavigation() = runTest {
        viewModel.handleAction(SavedAction.Open(record))
        assertTrue(viewModel.navigateToPlace.value)
        assertEquals("Fløyen", store.place?.name)
    }

    /**
     * Verifies delete removes the saved row from preferences.
     *
     * @author udit
     */
    @Test
    fun delete_removesSavedRecord() = runTest {
        viewModel.handleAction(SavedAction.Delete(record))
        coVerify(exactly = 1) { preferences.removeSaved(record) }
    }
}
