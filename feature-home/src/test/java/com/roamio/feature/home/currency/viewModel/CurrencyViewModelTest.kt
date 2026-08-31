package com.roamio.feature.home.currency.viewModel

import com.roamio.core.preferences.AppPreferences
import com.roamio.core.result.AppResult
import com.roamio.feature.home.currency.data.CurrencyRepository
import io.mockk.coEvery
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
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [CurrencyViewModel] conversion.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: CurrencyRepository = mockk()
    private val preferences: AppPreferences = mockk(relaxed = true)
    private lateinit var viewModel: CurrencyViewModel

    /**
     * Configures the main dispatcher and convert stub.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { preferences.homeCurrency } returns MutableStateFlow("USD")
        coEvery { repository.convert(1.0, "USD", "EUR") } returns AppResult.Success(0.92)
        viewModel = CurrencyViewModel(repository, preferences)
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
     * Verifies convert writes the Frankfurter result.
     *
     * @author udit
     */
    @Test
    fun convert_success_setsResult() = runTest {
        viewModel.handleAction(CurrencyAction.Convert)
        assertEquals(0.92, viewModel.uiState.value.result)
    }
}
