package com.psspl.autoreply.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.engine.MenuEngineResult
import com.psspl.autoreply.engine.MenuReplyEngine
import com.psspl.autoreply.engine.ReplyTimingEvaluator
import com.psspl.autoreply.engine.TimingDecision
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.KeywordRuleRepository
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import com.psspl.autoreply.repository.SupportedAppsRepository
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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "MessengerNLS"
        private const val REPLY_TYPE_MENU = "menu"
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

        AppLogger.d(TAG, "Notification posted — pkg=$appPackage | sender=$sender | msg=$message")

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
            val replyType = settings.replyType.lowercase()

            // 3. Branch on active reply type
            if (replyType == REPLY_TYPE_MENU) {
                handleMenuReply(sbn, appPackage, sender, message, contactKey)
            } else {
                handleKeywordReply(sbn, appPackage, sender, message, contactKey)
            }
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
        RemoteInput.addResultsToIntent(remoteInputs, replyIntent, bundle)

        return try {
            replyAction.actionIntent.send(this, 0, replyIntent)
            AppLogger.i(TAG, "Direct reply sent: '$replyText'")
            true
        } catch (e: PendingIntent.CanceledException) {
            AppLogger.e(TAG, "Reply PendingIntent was cancelled: ${e.message}")
            false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to send reply: ${e.message}")
            false
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
