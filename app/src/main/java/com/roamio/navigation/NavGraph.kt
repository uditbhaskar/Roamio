package com.roamio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roamio.feature.home.data.HomeWarmup
import com.roamio.feature.home.place.ui.PlaceScreenRoot
import com.roamio.feature.onboarding.ui.OnboardingScreenRoot
import com.roamio.feature.onboarding.ui.PrivacyPolicyScreenRoot
import com.roamio.ui.explore.ExploreShell
import com.roamio.util.AppConstants
import org.koin.compose.koinInject

/**
 * Root navigation graph for onboarding, the explore shell, and place detail.
 *
 * @param navController Controller used to drive Compose navigation.
 * @author udit
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    val warmup: HomeWarmup = koinInject()
    NavHost(
        navController = navController,
        startDestination = AppConstants.Nav.ONBOARD,
    ) {
        composable(AppConstants.Nav.ONBOARD) {
            LaunchedEffect(Unit) {
                warmup.prepare()
            }
            OnboardingScreenRoot(
                onNavigateNext = {
                    navController.navigate(AppConstants.Nav.MAIN) {
                        popUpTo(AppConstants.Nav.ONBOARD) { inclusive = true }
                    }
                },
                onOpenPrivacy = {
                    navController.navigate(AppConstants.Nav.PRIVACY)
                },
                navController = navController,
            )
        }
        composable(AppConstants.Nav.PRIVACY) {
            PrivacyPolicyScreenRoot(
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppConstants.Nav.MAIN) {
            ExploreShell(
                onOpenPlace = { navController.navigate(AppConstants.Nav.PLACE) },
            )
        }
        composable(AppConstants.Nav.PLACE) {
            PlaceScreenRoot(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
