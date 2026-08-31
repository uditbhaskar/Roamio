package com.roamio.feature.onboarding.viewModel

import com.roamio.core.preferences.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [OnboardingViewModel] action handling and persistence.
 *
 * @author udit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val preferences: AppPreferences = mockk(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    /**
     * Configures the main dispatcher and ViewModel under test.
     *
     * @author udit
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { preferences.setOnboardingCompleted(true) } returns Unit
        viewModel = OnboardingViewModel(preferences)
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
     * Verifies that [OnboardingAction.CONTINUE] triggers navigation and persists completion.
     *
     * @author udit
     */
    @Test
    fun handleAction_continue_setsNavigateNextAndPersists() = runTest {
        viewModel.handleAction(OnboardingAction.CONTINUE)
        assertTrue(viewModel.navigateNext.value)
        coVerify { preferences.setOnboardingCompleted(true) }
    }
}
