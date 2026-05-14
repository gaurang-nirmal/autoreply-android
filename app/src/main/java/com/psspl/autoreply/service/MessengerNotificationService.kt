package com.psspl.autoreply.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.psspl.autoreply.data.network.ApiService
import com.psspl.autoreply.data.network.model.AiReplyRequest
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.engine.MenuEngineResult
import com.psspl.autoreply.engine.MenuReplyEngine
import com.psspl.autoreply.engine.ReplyTimingEvaluator
import com.psspl.autoreply.engine.TimingDecision
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.KeywordRuleRepository
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import com.psspl.autoreply.repository.SpreadsheetRepository
import com.psspl.autoreply.repository.SupportedAppsRepository
import com.psspl.autoreply.repository.WelcomeMessageRepository
import com.psspl.autoreply.utils.AppLogger
import com.psspl.autoreply.utils.MatchType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MessengerNotificationService : NotificationListenerService() {

    @Inject
    lateinit var supportedAppsRepository: SupportedAppsRepository

    @Inject
    lateinit var replyNotificationsRepository: ReplyNotificationsRepository

    @Inject
    lateinit var keywordRuleRepository: KeywordRuleRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    @Inject
    lateinit var replyTimingEvaluator: ReplyTimingEvaluator

    @Inject
    lateinit var menuReplyEngine: MenuReplyEngine

    @Inject
    lateinit var welcomeMessageRepository: WelcomeMessageRepository

    @Inject
    lateinit var spreadsheetRepository: SpreadsheetRepository

    @Inject
    lateinit var apiService: ApiService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "MessengerNLS"
        private const val REPLY_TYPE_MENU = "menu"
        private const val REPLY_TYPE_SPREADSHEET = "spreadsheet"
        private const val REPLY_TYPE_AI = "ai_reply"

        // AI config is stored under appId=1 (global config set from Menu screen).
        // Change this when per-app AI configs are supported.
        private const val AI_CONFIG_APP_ID = 1
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val appPackage = sbn.packageName

        val extras = sbn.notification.extras ?: run {
            AppLogger.w(TAG, "Notification extras null for $appPackage, skipping")
            return
        }

        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: run {
            AppLogger.w(TAG, "Could not extract sender from notification ($appPackage)")
            return
        }
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: run {
            AppLogger.w(TAG, "Could not extract message text from notification ($appPackage)")
            return
        }

        logNotificationDetails(sbn, sender, message)

        serviceScope.launch {
            // 1. Global module toggle gate
            val settings = appSettingsRepository.get()
            if (settings?.isAutoReplyEnabled != true) {
                AppLogger.d(TAG, "Auto-reply module is OFF — skipping")
                return@launch
            }

            // 2. Check if this app is supported and enabled by the user
            if (!supportedAppsRepository.isAppEnabled(appPackage)) {
                AppLogger.d(TAG, "$appPackage is not enabled in Supported Apps — skipping")
                return@launch
            }

            val contactKey = "$appPackage:${sender.trim().lowercase()}"

            // 3. Welcome message — highest priority, checked before any other reply type
            val welcomeConfig = welcomeMessageRepository.getConfig().first()
            if (welcomeConfig != null && welcomeConfig.isEnabled) {
                if (welcomeMessageRepository.shouldSendWelcome(
                        appPackage,
                        contactKey,
                        welcomeConfig.cooldownDays
                    )
                ) {
                    handleWelcomeMessage(sbn, appPackage, sender, contactKey, welcomeConfig.message)
                    return@launch
                }
            }

            // 4. Branch on active reply type
            val replyType = settings.replyType.lowercase()
            when (replyType) {
                REPLY_TYPE_MENU -> handleMenuReply(sbn, appPackage, sender, message, contactKey)
                REPLY_TYPE_SPREADSHEET -> handleSpreadsheetReply(
                    sbn,
                    appPackage,
                    sender,
                    message,
                    contactKey,
                    settings
                )

                REPLY_TYPE_AI -> handleAiReply(sbn, appPackage, sender, message, contactKey)
                else -> handleKeywordReply(sbn, appPackage, sender, message, contactKey)
            }
        }
    }

    // ─── Welcome message flow ─────────────────────────────────────────────────

    private suspend fun handleWelcomeMessage(
        sbn: StatusBarNotification,
        appPackage: String,
        sender: String,
        contactKey: String,
        message: String,
    ) {
        AppLogger.d(TAG, "Welcome message — sending to $sender ($contactKey)")
        val sent = sendDirectReply(sbn, message)
        if (sent) {
            welcomeMessageRepository.recordWelcomeSent(appPackage, contactKey)
            replyNotificationsRepository.insert(
                ReplyNotificationEntity(
                    appPackage = appPackage,
                    senderName = sender,
                    replyText = message,
                )
            )
            AppLogger.d(TAG, "Welcome message sent and logged for $sender @ $appPackage")
        }
    }

    // ─── Menu reply flow ──────────────────────────────────────────────────────

    private suspend fun handleMenuReply(
        sbn: StatusBarNotification,
        appPackage: String,
        sender: String,
        message: String,
        contactKey: String,
    ) {
        AppLogger.d(TAG, "Menu reply mode — evaluating for $sender ($contactKey)")

        val result = menuReplyEngine.handle(contactKey, sender, message)

        if (result is MenuEngineResult.NoReply) {
            AppLogger.d(TAG, "Menu engine: no reply for $sender")
            return
        }

        val reply = result as MenuEngineResult.Reply

        // Apply timing gate only for new trigger matches — sub-menu navigation bypasses it
        // so the user's selection is never silently dropped mid-flow.
        if (reply.isNewSession) {
            when (val decision = replyTimingEvaluator.evaluate(contactKey)) {
                is TimingDecision.Block -> {
                    AppLogger.d(TAG, "Timing gate blocked initial menu reply to $sender")
                    return
                }

                is TimingDecision.Delay -> {
                    AppLogger.d(
                        TAG,
                        "Timing gate: delaying menu reply ${decision.seconds}s for $sender"
                    )
                    delay(decision.seconds * 1_000L)
                }

                is TimingDecision.Allow -> { /* proceed immediately */
                }
            }
        }

        val sent = sendDirectReply(sbn, reply.text)
        if (sent) {
            if (reply.isNewSession) replyTimingEvaluator.recordReply(contactKey)
            replyNotificationsRepository.insert(
                ReplyNotificationEntity(
                    appPackage = appPackage,
                    senderName = sender,
                    replyText = reply.text,
                )
            )
            AppLogger.d(TAG, "Menu reply sent and logged for $sender @ $appPackage")
        }
    }

    // ─── AI reply flow ────────────────────────────────────────────────────────

    private suspend fun handleAiReply(
        sbn: StatusBarNotification,
        appPackage: String,
        sender: String,
        message: String,
        contactKey: String,
    ) {
        AppLogger.d(TAG, "AI reply mode — requesting reply for '$message' from $sender")

        // MVP: single AI config saved under appId=1 from the Menu screen.
        // contactId already encodes the package name so history is per-contact.
        val appId = AI_CONFIG_APP_ID

        // Timing gate before making the network call
        when (val decision = replyTimingEvaluator.evaluate(contactKey)) {
            is TimingDecision.Block -> {
                AppLogger.d(TAG, "Timing gate blocked AI reply to $sender")
                return
            }

            is TimingDecision.Delay -> {
                AppLogger.d(TAG, "Timing gate: delaying AI reply ${decision.seconds}s for $sender")
                delay(decision.seconds * 1_000L)
            }

            is TimingDecision.Allow -> { /* proceed immediately */
            }
        }

        val aiReply = try {
            val response = apiService.getAiReply(
                AiReplyRequest(
                    appId = appId,
                    contactId = contactKey,
                    contactName = sender,
                    incomingMessage = message,
                    includeHistory = true,
                )
            )
            if (response.isSuccessful) {
                response.body()?.reply
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                AppLogger.w(
                    TAG,
                    "AI reply API returned ${response.code()} for appId=$appId — $errorBody"
                )
                if (response.code() == 404) {
                    AppLogger.w(
                        TAG,
                        "AI reply: no config for appId=$appId. Set up AI settings in the app first."
                    )
                }
                null
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "AI reply network error: ${e.message}")
            null
        }

        if (aiReply.isNullOrBlank()) {
            AppLogger.w(TAG, "AI reply was empty or null — not sending")
            return
        }

        AppLogger.i(TAG, "AI reply received — sending: '$aiReply'")

        val sent = sendDirectReply(sbn, aiReply)
        if (sent) {
            replyTimingEvaluator.recordReply(contactKey)
            replyNotificationsRepository.insert(
                ReplyNotificationEntity(
                    appPackage = appPackage,
                    senderName = sender,
                    replyText = aiReply,
                )
            )
            AppLogger.d(TAG, "AI reply sent and logged for $sender @ $appPackage")
        }
    }

    // ─── Keyword reply flow ───────────────────────────────────────────────────

    private suspend fun handleKeywordReply(
        sbn: StatusBarNotification,
        appPackage: String,
        sender: String,
        message: String,
        contactKey: String,
    ) {
        // 3. Load active keyword rules for this app (app-specific + global rules)
        val rules = keywordRuleRepository.getActiveForApp(appPackage).first()
        if (rules.isEmpty()) {
            AppLogger.d(TAG, "No active keyword rules for $appPackage — skipping")
            return
        }

        AppLogger.d(TAG, "Checking ${rules.size} rule(s) against message: '$message'")

        // 4. Find the first matching rule
        val matchedRule = rules.firstOrNull { rule -> rule.matches(message) }
        if (matchedRule == null) {
            AppLogger.d(TAG, "No keyword matched for message: '$message'")
            return
        }

        AppLogger.i(
            TAG,
            "Rule matched — keyword='${matchedRule.keyword}' → reply='${matchedRule.replyText}'"
        )

        // 5. Resolve reply text (substitute tags)
        val resolvedReply = resolveReplyText(
            template = matchedRule.replyText,
            sender = sender,
            message = message,
        )

        // 5.5 Timing / limit gate
        when (val decision = replyTimingEvaluator.evaluate(contactKey)) {
            is TimingDecision.Block -> {
                AppLogger.d(TAG, "Timing gate blocked reply to $sender ($contactKey)")
                return
            }

            is TimingDecision.Delay -> {
                AppLogger.d(TAG, "Timing gate: delaying reply ${decision.seconds}s for $sender")
                delay(decision.seconds * 1_000L)
            }

            is TimingDecision.Allow -> { /* proceed immediately */
            }
        }

        // 6. Send the reply
        val sent = sendDirectReply(sbn, resolvedReply)
        if (sent) {
            replyTimingEvaluator.recordReply(contactKey)
            replyNotificationsRepository.insert(
                ReplyNotificationEntity(
                    appPackage = appPackage,
                    senderName = sender,
                    replyText = resolvedReply,
                )
            )
            AppLogger.d(TAG, "Reply logged to history for $sender @ $appPackage")
        }
    }

    // ─── Spreadsheet reply flow ───────────────────────────────────────────────

    private suspend fun handleSpreadsheetReply(
        sbn: StatusBarNotification,
        appPackage: String,
        sender: String,
        message: String,
        contactKey: String,
        settings: com.psspl.autoreply.database.entity.AppSettingsEntity,
    ) {
        val rules = spreadsheetRepository.getAllRules()
        if (rules.isEmpty()) {
            AppLogger.d(TAG, "No spreadsheet rules cached — skipping spreadsheet reply")
            return
        }

        AppLogger.d(TAG, "Checking ${rules.size} spreadsheet rule(s) against message: '$message'")

        // Keyword matching: case-insensitive contains (sheets always use CONTAINS semantics)
        val matched = rules.firstOrNull { rule ->
            message.contains(rule.keyword.trim(), ignoreCase = true)
        }

        if (matched == null) {
            AppLogger.d(TAG, "No spreadsheet keyword matched for: '$message'")
            return
        }

        AppLogger.i(
            TAG,
            "Spreadsheet rule matched — keyword='${matched.keyword}' → reply='${matched.replyMessage}'"
        )

        // Resolve tags (same substitution as keyword reply)
        val resolvedReply = resolveReplyText(
            template = matched.replyMessage,
            sender = sender,
            message = message,
        )

        // Timing gate
        when (val decision = replyTimingEvaluator.evaluate(contactKey)) {
            is TimingDecision.Block -> {
                AppLogger.d(TAG, "Timing gate blocked spreadsheet reply to $sender")
                return
            }

            is TimingDecision.Delay -> {
                AppLogger.d(TAG, "Timing gate: delaying spreadsheet reply ${decision.seconds}s")
                delay(decision.seconds * 1_000L)
            }

            is TimingDecision.Allow -> { /* proceed */
            }
        }

        val sent = sendDirectReply(sbn, resolvedReply)
        if (sent) {
            replyTimingEvaluator.recordReply(contactKey)
            replyNotificationsRepository.insert(
                ReplyNotificationEntity(
                    appPackage = appPackage,
                    senderName = sender,
                    replyText = resolvedReply,
                )
            )
            AppLogger.d(TAG, "Spreadsheet reply logged for $sender @ $appPackage")

            // Auto-save reply to Google Sheet if enabled
            val saveSheetId = settings.spreadsheetSaveSheetId
            if (settings.isSpreadsheetAutoSave && saveSheetId.isNotEmpty()) {
                spreadsheetRepository.saveReplyToSheet(
                    spreadsheetId = saveSheetId,
                    appPackage = appPackage,
                    sender = sender,
                    receivedMessage = message,
                    replyText = resolvedReply,
                )
            }
        }
    }

    // ─── Rule Matching ────────────────────────────────────────────────────────

    private fun KeywordRuleEntity.matches(message: String): Boolean {
        return when (matchType) {
            MatchType.EXACT.name -> message.trim().equals(keyword.trim(), ignoreCase = true)
            MatchType.CONTAINS.name -> message.contains(keyword.trim(), ignoreCase = true)
            else -> message.contains(keyword.trim(), ignoreCase = true)
        }
    }

    // ─── Tag Substitution ─────────────────────────────────────────────────────

    private fun resolveReplyText(template: String, sender: String, message: String): String {
        val now = Date()
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val nameParts = sender.trim().split("\\s+".toRegex())
        val firstName = nameParts.firstOrNull() ?: sender
        val lastName = if (nameParts.size > 1) nameParts.last() else sender

        return template
            .replace("{name}", sender, ignoreCase = true)
            .replace("{first name}", firstName, ignoreCase = true)
            .replace("{last name}", lastName, ignoreCase = true)
            .replace("{date}", dateFmt.format(now), ignoreCase = true)
            .replace("{time}", timeFmt.format(now), ignoreCase = true)
            .replace("{message}", message, ignoreCase = true)
    }

    // ─── Direct Reply ─────────────────────────────────────────────────────────

    private fun sendDirectReply(sbn: StatusBarNotification, replyText: String): Boolean {
        val actions = sbn.notification.actions
        if (actions.isNullOrEmpty()) {
            AppLogger.w(TAG, "No actions on notification — cannot reply")
            return false
        }

        val replyAction = actions.firstOrNull { action ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    action.semanticAction == Notification.Action.SEMANTIC_ACTION_REPLY
        } ?: actions.firstOrNull { action ->
            action.remoteInputs?.isNotEmpty() == true
        } ?: run {
            AppLogger.w(
                TAG,
                "No RemoteInput reply action found — notification may not support direct reply"
            )
            logActions(actions)
            return false
        }

        val remoteInputs = replyAction.remoteInputs
        if (remoteInputs.isNullOrEmpty()) {
            AppLogger.w(TAG, "Reply action has no RemoteInputs")
            return false
        }

        AppLogger.d(
            TAG,
            "Using reply action '${replyAction.title}' | key=${remoteInputs[0].resultKey}"
        )

        val replyIntent = Intent()
        val bundle = Bundle()
        remoteInputs.forEach { ri -> bundle.putCharSequence(ri.resultKey, replyText) }

        // Mark reply as free-form keyboard input — some apps check this field (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            RemoteInput.setResultsSource(replyIntent, RemoteInput.SOURCE_FREE_FORM_INPUT)
        }
        RemoteInput.addResultsToIntent(remoteInputs, replyIntent, bundle)

        // Guard 1 — FLAG_IMMUTABLE (Android 12+):
        // If the target app set FLAG_IMMUTABLE on the PendingIntent, Android silently
        // discards our fillIn intent. The send() still returns without exception but
        // the reply text is never delivered. Detected for apps like LinkedIn/Instagram.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && replyAction.actionIntent.isImmutable) {
            AppLogger.e(TAG, "━━━ DIRECT REPLY BLOCKED (FLAG_IMMUTABLE) ━━━━━━━━━━━━━━━━━━━━")
            AppLogger.e(TAG, "  Package : ${sbn.packageName}")
            AppLogger.e(
                TAG,
                "  Reason  : PendingIntent is FLAG_IMMUTABLE — fill intent discarded by OS"
            )
            AppLogger.e(TAG, "  Fix     : Accessibility Service required for this app")
            AppLogger.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return false
        }

        return try {
            replyAction.actionIntent.send(this, 0, replyIntent)
            AppLogger.i(TAG, "Direct reply dispatched to ${sbn.packageName} — text='$replyText'")
            AppLogger.d(TAG, "  Note: send() returning true means the PendingIntent fired,")
            AppLogger.d(TAG, "  not that the message was delivered. If the app uses an")
            AppLogger.d(TAG, "  Activity PendingIntent, Android 10+ blocks it silently from")
            AppLogger.d(TAG, "  background — Accessibility Service is required in that case.")
            true
        } catch (e: PendingIntent.CanceledException) {
            AppLogger.e(
                TAG,
                "Reply PendingIntent was cancelled — notification may be stale: ${e.message}"
            )
            false
        } catch (e: SecurityException) {
            AppLogger.e(TAG, "━━━ DIRECT REPLY BLOCKED (SecurityException) ━━━━━━━━━━━━━━━━━━")
            AppLogger.e(TAG, "  Package : ${sbn.packageName}")
            AppLogger.e(TAG, "  Reason  : ${e.message}")
            AppLogger.e(TAG, "  Fix     : Accessibility Service required for this app")
            AppLogger.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to send reply to ${sbn.packageName}: ${e.message}")
            false
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun logNotificationDetails(
        sbn: StatusBarNotification,
        sender: String,
        message: String,
    ) {
        val n = sbn.notification
        val extras = n.extras

        // Basic identity
        AppLogger.d(TAG, "━━━ Notification Detected ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        AppLogger.d(TAG, "  Package   : ${sbn.packageName}")
        AppLogger.d(TAG, "  Notif ID  : ${sbn.id}")
        AppLogger.d(TAG, "  Key       : ${sbn.key}")
        AppLogger.d(
            TAG,
            "  Post time : ${
                java.text.SimpleDateFormat(
                    "HH:mm:ss.SSS",
                    java.util.Locale.getDefault()
                ).format(java.util.Date(sbn.postTime))
            }"
        )

        // Content
        AppLogger.d(TAG, "  Sender    : $sender")
        AppLogger.d(TAG, "  Message   : $message")
        extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.let {
            AppLogger.d(TAG, "  Sub-text  : $it")
        }
        extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.let {
            AppLogger.d(TAG, "  Summary   : $it")
        }

        // Notification metadata
        AppLogger.d(TAG, "  Category  : ${n.category ?: "none"}")
        AppLogger.d(TAG, "  Priority  : ${n.priority}")
        AppLogger.d(TAG, "  Flags     : 0x${n.flags.toString(16)} ${decodeFlagNames(n.flags)}")
        AppLogger.d(TAG, "  Group key : ${n.group ?: "none"}")
        n.tickerText?.let { AppLogger.d(TAG, "  Ticker    : $it") }

        // Actions & reply capability
        val actions = n.actions
        if (actions.isNullOrEmpty()) {
            AppLogger.d(TAG, "  Actions   : none — direct reply NOT possible")
        } else {
            AppLogger.d(TAG, "  Actions   : ${actions.size} available")
            actions.forEachIndexed { i, action ->
                val hasRemoteInput = action.remoteInputs?.isNotEmpty() == true
                val semanticTag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    " semantic=${action.semanticAction}"
                } else ""
                val replyTag = if (hasRemoteInput) " [REPLY-CAPABLE]" else ""
                AppLogger.d(TAG, "    [$i] '${action.title}'$semanticTag$replyTag")
                action.remoteInputs?.forEach { ri ->
                    AppLogger.d(
                        TAG,
                        "        RemoteInput key='${ri.resultKey}' label='${ri.label}'"
                    )
                }
            }
            val canReply = actions.any { it.remoteInputs?.isNotEmpty() == true }
            AppLogger.d(TAG, "  Can reply : $canReply")
        }
        AppLogger.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun decodeFlagNames(flags: Int): String {
        val names = buildList {
            if (flags and Notification.FLAG_SHOW_LIGHTS != 0) add("SHOW_LIGHTS")
            if (flags and Notification.FLAG_ONGOING_EVENT != 0) add("ONGOING")
            if (flags and Notification.FLAG_AUTO_CANCEL != 0) add("AUTO_CANCEL")
            if (flags and Notification.FLAG_NO_CLEAR != 0) add("NO_CLEAR")
            if (flags and Notification.FLAG_FOREGROUND_SERVICE != 0) add("FOREGROUND_SERVICE")
            if (flags and Notification.FLAG_GROUP_SUMMARY != 0) add("GROUP_SUMMARY")
        }
        return if (names.isEmpty()) "(none)" else "(${names.joinToString("|")})"
    }

    private fun logActions(actions: Array<Notification.Action>) {
        actions.forEachIndexed { i, action ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                AppLogger.d(
                    TAG,
                    "action[$i] title=${action.title} semanticAction=${action.semanticAction} remoteInputs=${action.remoteInputs?.size ?: 0}"
                )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // no-op for MVP
    }

    override fun onListenerConnected() {
        AppLogger.i(TAG, "NotificationListenerService connected")
    }

    override fun onListenerDisconnected() {
        AppLogger.w(TAG, "NotificationListenerService disconnected")
    }
}
