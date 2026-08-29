package com.roamio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roamio.feature.onboarding.ui.OnboardingScreenRoot
import com.roamio.util.AppConstants

/**
 * Root navigation graph. Only the first onboarding screen is wired while UI is rebuilt.
 *
 * @param navController Controller used to drive Compose navigation.
 * @author udit
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppConstants.Nav.ONBOARD,
    ) {
        composable(AppConstants.Nav.ONBOARD) {
            OnboardingScreenRoot(
                onNavigateNext = { },
                navController = navController,
            )
        }
    }
}
