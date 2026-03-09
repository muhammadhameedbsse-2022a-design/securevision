package com.securevision.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.securevision.feature.alerts.AlertsScreen
import com.securevision.feature.dashboard.DashboardScreen
import com.securevision.feature.history.HistoryScreen
import com.securevision.feature.live.LiveScreen
import com.securevision.feature.profiles.ProfilesScreen
import com.securevision.feature.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Live : Screen("live")
    object Profiles : Screen("profiles")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Alerts : Screen("alerts")
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToLive = { navController.navigate(Screen.Live.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfiles = { navController.navigate(Screen.Profiles.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Live.route) {
            LiveScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profiles.route) {
            ProfilesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Alerts.route) {
            AlertsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
