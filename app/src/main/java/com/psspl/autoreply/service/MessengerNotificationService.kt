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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "MessengerNLS"
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
            val isModuleEnabled = appSettingsRepository.get()?.isAutoReplyEnabled ?: false
            if (!isModuleEnabled) {
                AppLogger.d(TAG, "Auto-reply module is OFF — skipping")
                return@launch
            }

            // 2. Check if this app is supported and enabled by the user
            if (!supportedAppsRepository.isAppEnabled(appPackage)) {
                AppLogger.d(TAG, "$appPackage is not enabled in Supported Apps — skipping")
                return@launch
            }

            // 3. Load active keyword rules for this app (app-specific + global rules)
            val rules = keywordRuleRepository.getActiveForApp(appPackage).first()
            if (rules.isEmpty()) {
                AppLogger.d(TAG, "No active keyword rules for $appPackage — skipping")
                return@launch
            }

            AppLogger.d(TAG, "Checking ${rules.size} rule(s) against message: '$message'")

            // 4. Find the first matching rule
            val matchedRule = rules.firstOrNull { rule -> rule.matches(message) }
            if (matchedRule == null) {
                AppLogger.d(TAG, "No keyword matched for message: '$message'")
                return@launch
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

            // 6. Send the reply
            val sent = sendDirectReply(sbn, resolvedReply)
            if (sent) {
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

    /**
     * Replaces supported placeholders in [template]:
     *  {name}        → full sender name
     *  {first name}  → first word of sender name
     *  {last name}   → last word of sender name
     *  {date}        → current date (dd MMM yyyy)
     *  {time}        → current time (hh:mm a)
     *  {message}     → original incoming message text
     */
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

        // Prefer the action explicitly marked as Reply (API 28+); fall back to any RemoteInput action
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
