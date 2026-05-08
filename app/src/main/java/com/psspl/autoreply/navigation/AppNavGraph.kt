package com.psspl.autoreply.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.psspl.autoreply.ui.screens.dashboard.DashboardScreen
import com.psspl.autoreply.ui.screens.menu.MenuScreen
import com.psspl.autoreply.ui.screens.rules.RulesScreen
import com.psspl.autoreply.ui.screens.settings.SettingsScreen
import com.psspl.autoreply.ui.screens.supportedapps.SupportedAppsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        modifier = modifier,
    ) {
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreen()
        }
        composable(BottomNavItem.Menu.route) {
            MenuScreen()
        }
        composable(BottomNavItem.Rules.route) {
            RulesScreen()
        }
        composable(BottomNavItem.SupportedApps.route) {
            SupportedAppsScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(onSignOut = onSignOut)
        }
    }
}
