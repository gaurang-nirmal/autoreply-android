package com.psspl.autoreply.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Dashboard : BottomNavItem(
        route = "dashboard",
        label = "Dashboard",
        icon = Icons.Filled.Home,
    )

    // Menu sits immediately next to Dashboard (bottom-left)
    data object Menu : BottomNavItem(
        route = "menu",
        label = "Menu",
        icon = Icons.Filled.Apps,
    )

    data object Rules : BottomNavItem(
        route = "rules",
        label = "Rules",
        icon = Icons.Filled.Edit,
    )

    data object SupportedApps : BottomNavItem(
        route = "supported_apps",
        label = "Apps",
        icon = Icons.Filled.PhoneAndroid,   // changed from hamburger → phone icon
    )

    data object Settings : BottomNavItem(
        route = "settings",
        label = "Settings",
        icon = Icons.Filled.Settings,
    )

    companion object {
        val items = listOf(Dashboard, Menu, Rules, SupportedApps, Settings)
    }
}
