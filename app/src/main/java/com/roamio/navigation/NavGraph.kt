package com.roamio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roamio.feature.onboarding.ui.OnboardingScreenRoot
import com.roamio.util.AppConstants

/**
 * Sets up the app's navigation graph with onboarding and home destinations.
 * @author udit
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = AppConstants.NAV_ONBOARD) {
        composable(AppConstants.NAV_ONBOARD) {
            OnboardingScreenRoot(onNavigateNext = {}, navController)
        }
    }
}