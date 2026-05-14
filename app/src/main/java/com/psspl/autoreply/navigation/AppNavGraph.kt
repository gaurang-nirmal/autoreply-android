@file:Suppress("NOTHING_TO_INLINE")

package com.psspl.autoreply.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.psspl.autoreply.ui.screens.ai.AiParametersScreen
import com.psspl.autoreply.ui.screens.ai.AiReplyScreen
import com.psspl.autoreply.ui.screens.ai.AiSettingsScreen
import com.psspl.autoreply.ui.screens.ai.AiTextPromptScreen
import com.psspl.autoreply.ui.screens.ai.TrainAiScreen
import com.psspl.autoreply.ui.screens.appsecurity.AppSecurityScreen
import com.psspl.autoreply.ui.screens.automaticon.AutomaticOnScreen
import com.psspl.autoreply.ui.screens.autoreplyconfig.AutoReplyConfigScreen
import com.psspl.autoreply.ui.screens.backuprestore.BackupRestoreScreen
import com.psspl.autoreply.ui.screens.dashboard.DashboardScreen
import com.psspl.autoreply.ui.screens.directmessage.DirectMessageScreen
import com.psspl.autoreply.ui.screens.display.DisplayScreen
import com.psspl.autoreply.ui.screens.followup.FollowUpHistoryScreen
import com.psspl.autoreply.ui.screens.followup.FollowUpManageScreen
import com.psspl.autoreply.ui.screens.followup.FollowUpMessageScreen
import com.psspl.autoreply.ui.screens.help.HelpScreen
import com.psspl.autoreply.ui.screens.invitefriend.InviteFriendScreen
import com.psspl.autoreply.ui.screens.menu.MenuScreen
import com.psspl.autoreply.ui.screens.menureply.AddEditMenuReplyScreen
import com.psspl.autoreply.ui.screens.menureply.MenuReplyItemChildrenScreen
import com.psspl.autoreply.ui.screens.menureply.MenuReplyMoreOptionsScreen
import com.psspl.autoreply.ui.screens.menureply.MenuReplyScreen
import com.psspl.autoreply.ui.screens.notes.NoteEditorScreen
import com.psspl.autoreply.ui.screens.notes.NotesScreen
import com.psspl.autoreply.ui.screens.notworking.NotWorkingScreen
import com.psspl.autoreply.ui.screens.replyheaderfooter.ReplyHeaderFooterScreen
import com.psspl.autoreply.ui.screens.replynotifications.ReplyNotificationsScreen
import com.psspl.autoreply.ui.screens.replytime.ReplyTimeScreen
import com.psspl.autoreply.ui.screens.replytiming.ReplyLimitListScreen
import com.psspl.autoreply.ui.screens.replytiming.ReplyTimingScreen
import com.psspl.autoreply.ui.screens.rules.KeywordReplyFormScreen
import com.psspl.autoreply.ui.screens.rules.RulesScreen
import com.psspl.autoreply.ui.screens.settings.SettingsScreen
import com.psspl.autoreply.ui.screens.spreadsheet.AddSpreadsheetScreen
import com.psspl.autoreply.ui.screens.spreadsheet.SpreadsheetScreen
import com.psspl.autoreply.ui.screens.spreadsheet.ViewSpreadsheetScreen
import com.psspl.autoreply.ui.screens.supportedapps.SupportedAppsScreen
import com.psspl.autoreply.ui.screens.upgrade.UpgradeScreen
import com.psspl.autoreply.ui.screens.welcomemessage.WelcomeMessageEditScreen
import com.psspl.autoreply.ui.screens.welcomemessage.WelcomeMessageScreen

// URL encoding helpers for nav args that may contain special characters (sheet names)
private inline fun String.encodeUrl(): String = java.net.URLEncoder.encode(this, "UTF-8")
private inline fun String.decodeUrl(): String = java.net.URLDecoder.decode(this, "UTF-8")

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
private const val ROUTE_WELCOME_MESSAGE = "welcome_message"
private const val ROUTE_WELCOME_MESSAGE_EDIT = "welcome_message_edit"
private const val ROUTE_NOTES = "notes"
private const val ROUTE_NOTE_EDITOR = "note_editor"
private const val ROUTE_REPLY_TIMING = "reply_timing"
private const val ROUTE_REPLY_LIMIT_LIST = "reply_limit_list"
private const val ROUTE_AUTO_REPLY_CONFIG = "auto_reply_config"
private const val ROUTE_FOLLOW_UP_MESSAGE = "follow_up_message"
private const val ROUTE_FOLLOW_UP_HISTORY = "follow_up_history"
private const val ROUTE_FOLLOW_UP_MANAGE = "follow_up_manage"
private const val ROUTE_SPREADSHEET = "spreadsheet"
private const val ROUTE_SPREADSHEET_ADD = "spreadsheet_add"
private const val ROUTE_SPREADSHEET_VIEW = "spreadsheet_view"

object AppRoutes {
    const val AI_REPLY = "ai_reply/{appId}"
    fun aiReply(appId: Int = 1) = "ai_reply/$appId"

    const val TRAIN_AI = "train_ai/{appId}"
    const val AI_SETTINGS = "ai_settings/{appId}"
    const val AI_PARAMETERS = "ai_parameters/{appId}"
    const val TRAIN_AI_PROMPT = "train_ai_prompt/{appId}?promptId={promptId}"

    fun trainAi(appId: Int = 1) = "train_ai/$appId"
    fun aiSettings(appId: Int = 1) = "ai_settings/$appId"
    fun aiParameters(appId: Int = 1) = "ai_parameters/$appId"
    fun addPrompt(appId: Int = 1) = "train_ai_prompt/$appId"
    fun editPrompt(appId: Int = 1, promptId: String) = "train_ai_prompt/$appId?promptId=$promptId"
}

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
                onNavigateToAutoReplyConfig = {
                    navController.navigate(ROUTE_AUTO_REPLY_CONFIG)
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
                onNavigateToWelcomeMessage = {
                    navController.navigate(ROUTE_WELCOME_MESSAGE)
                },
                onNavigateToNotes = {
                    navController.navigate(ROUTE_NOTES)
                },
                onNavigateToSpreadsheet = {
                    navController.navigate(ROUTE_SPREADSHEET)
                },
                onNavigateToAiReply = {
                    navController.navigate(AppRoutes.aiReply())
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
                onNavigateToReplyTiming = {
                    navController.navigate("$ROUTE_REPLY_TIMING/keyword")
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
                onNavigateToReplyTiming = {
                    navController.navigate("$ROUTE_REPLY_TIMING/menu")
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

        // ── Notes ─────────────────────────────────────────────────────────────
        composable(ROUTE_NOTES) {
            NotesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddNote = {
                    navController.navigate("$ROUTE_NOTE_EDITOR/0")
                },
                onNavigateToEditNote = { noteId ->
                    navController.navigate("$ROUTE_NOTE_EDITOR/$noteId")
                },
            )
        }
        composable(
            route = "$ROUTE_NOTE_EDITOR/{noteId}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            NoteEditorScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() },
            )
        }

        // ── Welcome Message ───────────────────────────────────────────────────
        composable(ROUTE_WELCOME_MESSAGE) {
            WelcomeMessageScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEditMessage = {
                    navController.navigate(ROUTE_WELCOME_MESSAGE_EDIT)
                },
            )
        }
        composable(ROUTE_WELCOME_MESSAGE_EDIT) {
            WelcomeMessageEditScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Reply Timing ──────────────────────────────────────────────────────
        composable(
            route = "$ROUTE_REPLY_TIMING/{replyType}",
            arguments = listOf(
                navArgument("replyType") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val replyType = backStackEntry.arguments?.getString("replyType") ?: "keyword"
            ReplyTimingScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLimitList = {
                    navController.navigate("$ROUTE_REPLY_LIMIT_LIST/$replyType")
                },
            )
        }
        composable(
            route = "$ROUTE_REPLY_LIMIT_LIST/{replyType}",
            arguments = listOf(
                navArgument("replyType") { type = NavType.StringType },
            ),
        ) {
            ReplyLimitListScreen(onBack = { navController.popBackStack() })
        }

        // ── Auto Reply Config ─────────────────────────────────────────────────
        composable(ROUTE_AUTO_REPLY_CONFIG) {
            AutoReplyConfigScreen(
                onBack = { navController.popBackStack() },
                // key = lowercase of active ReplyType enum, e.g. "keyword", "menu", "custom"
                onNavigateToReplyTiming = { key ->
                    navController.navigate("$ROUTE_REPLY_TIMING/$key")
                },
                onNavigateToFollowUp = {
                    navController.navigate(ROUTE_FOLLOW_UP_MESSAGE)
                },
            )
        }

        // ── Spreadsheet ───────────────────────────────────────────────────────
        composable(ROUTE_SPREADSHEET) {
            SpreadsheetScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddSpreadsheet = {
                    navController.navigate(ROUTE_SPREADSHEET_ADD)
                },
                onNavigateToViewSpreadsheet = { spreadsheetId, sheetName ->
                    navController.navigate("$ROUTE_SPREADSHEET_VIEW/$spreadsheetId/${sheetName.encodeUrl()}")
                },
                onNavigateToReplyTiming = {
                    navController.navigate("$ROUTE_REPLY_TIMING/spreadsheet")
                },
            )
        }
        composable(ROUTE_SPREADSHEET_ADD) {
            AddSpreadsheetScreen(
                onBack = { navController.popBackStack() },
                onSheetLinked = {
                    // Pop back to SpreadsheetScreen after linking
                    navController.popBackStack(ROUTE_SPREADSHEET, inclusive = false)
                },
            )
        }
        composable(
            route = "$ROUTE_SPREADSHEET_VIEW/{spreadsheetId}/{sheetName}",
            arguments = listOf(
                navArgument("spreadsheetId") { type = NavType.StringType },
                navArgument("sheetName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val spreadsheetId = backStackEntry.arguments?.getString("spreadsheetId") ?: ""
            val sheetName = backStackEntry.arguments?.getString("sheetName")?.decodeUrl() ?: ""
            ViewSpreadsheetScreen(
                spreadsheetId = spreadsheetId,
                sheetName = sheetName,
                onBack = { navController.popBackStack() },
            )
        }

        // ── Follow-Up Message ─────────────────────────────────────────────────
        composable(ROUTE_FOLLOW_UP_MESSAGE) {
            FollowUpMessageScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHistory = {
                    navController.navigate(ROUTE_FOLLOW_UP_HISTORY)
                },
                onNavigateToManage = {
                    navController.navigate(ROUTE_FOLLOW_UP_MANAGE)
                },
            )
        }
        composable(ROUTE_FOLLOW_UP_HISTORY) {
            FollowUpHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_FOLLOW_UP_MANAGE) {
            FollowUpManageScreen(onBack = { navController.popBackStack() })
        }

        // ── AI screens ────────────────────────────────────────────────────────
        composable(
            route = AppRoutes.AI_REPLY,
            arguments = listOf(navArgument("appId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt("appId") ?: 1
            AiReplyScreen(
                appId = appId,
                onBack = { navController.popBackStack() },
                onNavigateToTrainAi = { navController.navigate(AppRoutes.trainAi(appId)) },
                onNavigateToAiSettings = { navController.navigate(AppRoutes.aiSettings(appId)) },
                onNavigateToAiParameters = { navController.navigate(AppRoutes.aiParameters(appId)) },
            )
        }

        composable(
            route = AppRoutes.AI_SETTINGS,
            arguments = listOf(navArgument("appId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt("appId") ?: 1
            AiSettingsScreen(
                appId = appId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = AppRoutes.TRAIN_AI,
            arguments = listOf(navArgument("appId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt("appId") ?: 1
            TrainAiScreen(
                appId = appId,
                onBack = { navController.popBackStack() },
                onNavigateToAddPrompt = { navController.navigate(AppRoutes.addPrompt(appId)) },
                onNavigateToEditPrompt = { promptId ->
                    navController.navigate(
                        AppRoutes.editPrompt(
                            appId,
                            promptId
                        )
                    )
                },
            )
        }

        composable(
            route = AppRoutes.AI_PARAMETERS,
            arguments = listOf(navArgument("appId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt("appId") ?: 1
            AiParametersScreen(
                appId = appId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = AppRoutes.TRAIN_AI_PROMPT,
            arguments = listOf(
                navArgument("appId") { type = NavType.IntType },
                navArgument("promptId") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt("appId") ?: 1
            val promptId = backStackEntry.arguments?.getString("promptId")
            AiTextPromptScreen(
                appId = appId,
                promptId = promptId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
