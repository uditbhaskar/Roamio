package com.roamio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roamio.feature.onboarding.ui.OnboardingScreen

/**
 * Sets up the app's navigation graph with onboarding and home destinations.
 * @author udit
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("onboard") {
            OnboardingScreen(onContinue = { navController.navigate("home") })
        }
    }
}