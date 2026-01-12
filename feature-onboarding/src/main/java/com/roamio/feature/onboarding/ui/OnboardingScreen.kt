package com.roamio.feature.onboarding.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.roamio.feature.onboarding.viewModel.OnboardingAction
import com.roamio.feature.onboarding.viewModel.OnboardingUiState
import com.roamio.feature.onboarding.viewModel.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Displays the onboarding screen content and handles user actions.
 *
 * @param state The current UI state for onboarding.
 * @param onAction Callback invoked when a user action occurs.
 * @author udit
 */
@Composable
fun OnboardingScreenContent(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(state.welcomeMessage)
            Button(onClick = { onAction(OnboardingAction.CONTINUE) }) {
                Text("Continue")
            }
        }
    }
}

/**
 * Root composable for onboarding screen.
 *
 * @param onNavigateNext Callback triggered when navigation to the next screen should occur.
 * @param viewModel The ViewModel managing onboarding state and actions.
 * @author udit
 */
@Composable
fun OnboardingScreenRoot(
    onNavigateNext: () -> Unit,
    navController: NavHostController
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val viewModel: OnboardingViewModel = navBackStackEntry?.let {
        koinViewModel(viewModelStoreOwner = it)
    } ?: koinViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateNext by viewModel.navigateNext.collectAsStateWithLifecycle()

    LaunchedEffect(navigateNext) {
        if (navigateNext) {
            onNavigateNext()
            viewModel.resetNavigation()
        }
    }

    OnboardingScreenContent(
        state = uiState,
        onAction = { viewModel.handleAction(it) }
    )
}
