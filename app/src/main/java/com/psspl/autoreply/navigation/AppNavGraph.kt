package com.psspl.autoreply.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.psspl.autoreply.ui.screens.appsecurity.AppSecurityScreen
import com.psspl.autoreply.ui.screens.automaticon.AutomaticOnScreen
import com.psspl.autoreply.ui.screens.backuprestore.BackupRestoreScreen
import com.psspl.autoreply.ui.screens.dashboard.DashboardScreen
import com.psspl.autoreply.ui.screens.directmessage.DirectMessageScreen
import com.psspl.autoreply.ui.screens.display.DisplayScreen
import com.psspl.autoreply.ui.screens.help.HelpScreen
import com.psspl.autoreply.ui.screens.invitefriend.InviteFriendScreen
import com.psspl.autoreply.ui.screens.menu.MenuScreen
import com.psspl.autoreply.ui.screens.menureply.AddEditMenuReplyScreen
import com.psspl.autoreply.ui.screens.menureply.MenuReplyItemChildrenScreen
import com.psspl.autoreply.ui.screens.menureply.MenuReplyMoreOptionsScreen
import com.psspl.autoreply.ui.screens.menureply.MenuReplyScreen
import com.psspl.autoreply.ui.screens.notworking.NotWorkingScreen
import com.psspl.autoreply.ui.screens.replyheaderfooter.ReplyHeaderFooterScreen
import com.psspl.autoreply.ui.screens.replynotifications.ReplyNotificationsScreen
import com.psspl.autoreply.ui.screens.replytime.ReplyTimeScreen
import com.psspl.autoreply.ui.screens.rules.KeywordReplyFormScreen
import com.psspl.autoreply.ui.screens.rules.RulesScreen
import com.psspl.autoreply.ui.screens.settings.SettingsScreen
import com.psspl.autoreply.ui.screens.supportedapps.SupportedAppsScreen
import com.psspl.autoreply.ui.screens.upgrade.UpgradeScreen

private const val ROUTE_REPLY_NOTIFICATIONS = "reply_notifications"
private const val ROUTE_DIRECT_MESSAGE = "direct_message"
private const val ROUTE_NOT_WORKING = "not_working"
private const val ROUTE_HELP = "help"
private const val ROUTE_UPGRADE = "upgrade"
private const val ROUTE_AUTOMATIC_ON = "setting_automatic_on"
private const val ROUTE_REPLY_TIME = "setting_reply_time"
private const val ROUTE_REPLY_HEADER_FOOTER = "setting_reply_header_footer"
private const val ROUTE_BACKUP_RESTORE = "setting_backup_restore"
private const val ROUTE_APP_SECURITY = "setting_app_security"
private const val ROUTE_DISPLAY = "setting_display"
private const val ROUTE_INVITE_FRIEND = "setting_invite_friend"
private const val ROUTE_KEYWORD_REPLY_FORM = "keyword_reply_form"
private const val ROUTE_MENU_REPLY = "menu_reply"
private const val ROUTE_MENU_REPLY_FORM = "menu_reply_form"
private const val ROUTE_MENU_REPLY_MORE_OPTIONS = "menu_reply_more_options"
private const val ROUTE_MENU_REPLY_CHILDREN = "menu_reply_children"

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
            DashboardScreen(
                onNavigateToReplyNotifications = {
                    navController.navigate(ROUTE_REPLY_NOTIFICATIONS)
                },
                onNavigateToDirectMessage = {
                    navController.navigate(ROUTE_DIRECT_MESSAGE)
                },
                onNavigateToNotWorking = {
                    navController.navigate(ROUTE_NOT_WORKING)
                },
                onNavigateToHelp = {
                    navController.navigate(ROUTE_HELP)
                },
                onNavigateToSettings = {
                    navController.navigate(BottomNavItem.Settings.route)
                },
                onNavigateToUpgrade = {
                    navController.navigate(ROUTE_UPGRADE)
                },
            )
        }
        composable(BottomNavItem.Menu.route) {
            MenuScreen(
                onNavigateToSupportedApps = {
                    navController.navigate(BottomNavItem.SupportedApps.route)
                },
                onNavigateToKeywordReply = {
                    navController.navigate(BottomNavItem.Rules.route)
                },
                onNavigateToMenuReply = {
                    navController.navigate(ROUTE_MENU_REPLY)
                },
            )
        }
        composable(BottomNavItem.Rules.route) {
            RulesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddRule = {
                    navController.navigate("$ROUTE_KEYWORD_REPLY_FORM/0")
                },
                onNavigateToEditRule = { ruleId ->
                    navController.navigate("$ROUTE_KEYWORD_REPLY_FORM/$ruleId")
                },
            )
        }
        composable(
            route = "$ROUTE_KEYWORD_REPLY_FORM/{ruleId}",
            arguments = listOf(
                navArgument("ruleId") { type = NavType.IntType; defaultValue = 0 }
            ),
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getInt("ruleId") ?: 0
            KeywordReplyFormScreen(
                ruleId = ruleId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(BottomNavItem.SupportedApps.route) {
            SupportedAppsScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                onSignOut = onSignOut,
                onNavigateToAutomaticOn = { navController.navigate(ROUTE_AUTOMATIC_ON) },
                onNavigateToReplyTime = { navController.navigate(ROUTE_REPLY_TIME) },
                onNavigateToReplyHeaderFooter = { navController.navigate(ROUTE_REPLY_HEADER_FOOTER) },
                onNavigateToBackupRestore = { navController.navigate(ROUTE_BACKUP_RESTORE) },
                onNavigateToAppSecurity = { navController.navigate(ROUTE_APP_SECURITY) },
                onNavigateToDisplay = { navController.navigate(ROUTE_DISPLAY) },
                onNavigateToInviteFriend = { navController.navigate(ROUTE_INVITE_FRIEND) },
            )
        }
        composable(ROUTE_REPLY_NOTIFICATIONS) {
            ReplyNotificationsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_DIRECT_MESSAGE) {
            DirectMessageScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_NOT_WORKING) {
            NotWorkingScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_HELP) {
            HelpScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_UPGRADE) {
            UpgradeScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_AUTOMATIC_ON) {
            AutomaticOnScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_REPLY_TIME) {
            ReplyTimeScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_REPLY_HEADER_FOOTER) {
            ReplyHeaderFooterScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_BACKUP_RESTORE) {
            BackupRestoreScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_APP_SECURITY) {
            AppSecurityScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_DISPLAY) {
            DisplayScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_INVITE_FRIEND) {
            InviteFriendScreen(onBack = { navController.popBackStack() })
        }

        // ── Menu Reply ────────────────────────────────────────────────────────
        composable(ROUTE_MENU_REPLY) {
            MenuReplyScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddTrigger = {
                    navController.navigate("$ROUTE_MENU_REPLY_FORM/0/0/-1")
                },
                onNavigateToEditTrigger = { replyId ->
                    navController.navigate("$ROUTE_MENU_REPLY_FORM/$replyId/0/-1")
                },
                onNavigateToAddItem = { menuReplyId, parentItemId ->
                    navController.navigate("$ROUTE_MENU_REPLY_FORM/$menuReplyId/0/$parentItemId")
                },
                onNavigateToEditItem = { menuReplyId, itemId ->
                    navController.navigate("$ROUTE_MENU_REPLY_FORM/$menuReplyId/$itemId/-1")
                },
                onNavigateToMoreOptions = { itemId ->
                    navController.navigate("$ROUTE_MENU_REPLY_MORE_OPTIONS/$itemId")
                },
                onNavigateToItemChildren = { menuReplyId, itemId ->
                    navController.navigate("$ROUTE_MENU_REPLY_CHILDREN/$menuReplyId/$itemId")
                },
            )
        }

        // ── Menu Reply children (recursive — same route re-used at every level) ──
        composable(
            route = "$ROUTE_MENU_REPLY_CHILDREN/{menuReplyId}/{parentItemId}",
            arguments = listOf(
                navArgument("menuReplyId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("parentItemId") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val menuReplyId = backStackEntry.arguments?.getInt("menuReplyId") ?: 0
            val parentItemId = backStackEntry.arguments?.getInt("parentItemId") ?: 0
            MenuReplyItemChildrenScreen(
                menuReplyId = menuReplyId,
                parentItemId = parentItemId,
                onBack = { navController.popBackStack() },
                onNavigateToChildren = { replyId, itemId ->
                    // Navigate to the same route — creates a new back-stack entry
                    navController.navigate("$ROUTE_MENU_REPLY_CHILDREN/$replyId/$itemId")
                },
                onNavigateToAddItem = { replyId, parentId ->
                    navController.navigate("$ROUTE_MENU_REPLY_FORM/$replyId/0/$parentId")
                },
                onNavigateToEditItem = { replyId, itemId ->
                    navController.navigate("$ROUTE_MENU_REPLY_FORM/$replyId/$itemId/-1")
                },
                onNavigateToMoreOptions = { itemId ->
                    navController.navigate("$ROUTE_MENU_REPLY_MORE_OPTIONS/$itemId")
                },
            )
        }

        composable(
            route = "$ROUTE_MENU_REPLY_FORM/{menuReplyId}/{itemId}/{parentItemId}",
            arguments = listOf(
                navArgument("menuReplyId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("itemId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("parentItemId") { type = NavType.IntType; defaultValue = -1 },
            ),
        ) { backStackEntry ->
            val menuReplyId = backStackEntry.arguments?.getInt("menuReplyId") ?: 0
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val parentItemId = backStackEntry.arguments?.getInt("parentItemId") ?: -1
            AddEditMenuReplyScreen(
                menuReplyId = menuReplyId,
                itemId = itemId,
                parentItemId = parentItemId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "$ROUTE_MENU_REPLY_MORE_OPTIONS/{itemId}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            MenuReplyMoreOptionsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
