package com.roamio.feature.home.settings.viewModel

import com.roamio.core.preferences.AppPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SettingsViewModel] persistence.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val preferences: AppPreferences = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    /**
     * Configures the main dispatcher and preference flows.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { preferences.displayName } returns MutableStateFlow("Morgan")
        every { preferences.useCelsius } returns MutableStateFlow(true)
        every { preferences.homeCurrency } returns MutableStateFlow("USD")
        viewModel = SettingsViewModel(preferences)
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
     * Verifies the greeting name is collected.
     *
     * @author udit
     */
    @Test
    fun state_exposesDisplayName() = runTest {
        assertEquals("Morgan", viewModel.uiState.value.displayName)
    }

    /**
     * Verifies a name edit is persisted.
     *
     * @author udit
     */
    @Test
    fun nameChanged_writesPreferences() = runTest {
        viewModel.handleAction(SettingsAction.NameChanged("Ada"))
        advanceTimeBy(500)
        runCurrent()
        coVerify { preferences.setDisplayName("Ada") }
    }

    /**
     * Verifies chip edits surface a saved confirmation.
     *
     * @author udit
     */
    @Test
    fun currencyChanged_showsSavedHint() = runTest {
        viewModel.handleAction(SettingsAction.CurrencyChanged("EUR"))
        assertTrue(viewModel.uiState.value.showSavedHint)
    }
}
