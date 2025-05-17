package com.roamio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") {  }
        composable("weather") { }
        // Add more destinations as needed
    }
}