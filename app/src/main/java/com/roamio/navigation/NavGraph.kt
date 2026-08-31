package com.roamio.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roamio.feature.home.data.HomeWarmup
import com.roamio.feature.home.place.ui.PlaceScreenRoot
import com.roamio.feature.home.ui.motion.RoamioMotion
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
        composable(
            route = AppConstants.Nav.ONBOARD,
            exitTransition = {
                fadeOut(RoamioMotion.screenTween) +
                    scaleOut(targetScale = 0.96f, animationSpec = RoamioMotion.screenTween)
            },
        ) {
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
        composable(
            route = AppConstants.Nav.PRIVACY,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(RoamioMotion.SCREEN_MS, easing = FastOutSlowInEasing),
                ) + fadeIn(RoamioMotion.screenTween)
            },
            exitTransition = {
                fadeOut(RoamioMotion.tabFadeOut)
            },
            popEnterTransition = {
                fadeIn(RoamioMotion.tabFadeIn)
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(RoamioMotion.SCREEN_MS, easing = FastOutSlowInEasing),
                ) + fadeOut(RoamioMotion.screenTween)
            },
        ) {
            PrivacyPolicyScreenRoot(
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = AppConstants.Nav.MAIN,
            enterTransition = {
                fadeIn(RoamioMotion.mainEnterTween) +
                    scaleIn(
                        initialScale = 0.94f,
                        animationSpec = RoamioMotion.mainEnterTween,
                    )
            },
        ) {
            ExploreShell(
                onOpenPlace = { navController.navigate(AppConstants.Nav.PLACE) },
            )
        }
        composable(
            route = AppConstants.Nav.PLACE,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(RoamioMotion.SCREEN_MS, easing = FastOutSlowInEasing),
                ) + fadeIn(RoamioMotion.screenTween)
            },
            exitTransition = {
                fadeOut(RoamioMotion.tabFadeOut)
            },
            popEnterTransition = {
                fadeIn(RoamioMotion.tabFadeIn)
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(RoamioMotion.SCREEN_MS, easing = FastOutSlowInEasing),
                ) + fadeOut(RoamioMotion.screenTween)
            },
        ) {
            PlaceScreenRoot(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
