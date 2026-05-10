package com.psspl.autoreply.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import com.psspl.autoreply.repository.SupportedAppsRepository
import com.psspl.autoreply.utils.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessengerNotificationService : NotificationListenerService() {

    @Inject
    lateinit var supportedAppsRepository: SupportedAppsRepository

    @Inject
    lateinit var replyNotificationsRepository: ReplyNotificationsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "MessengerNLS"
        private const val MESSENGER_PACKAGE = "com.facebook.orca"
        private const val KEYWORD = "hello"
        private const val REPLY_TEXT = "Hi there"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != MESSENGER_PACKAGE) return

        val extras = sbn.notification.extras ?: run {
            AppLogger.w(TAG, "Notification extras null, skipping")
            return
        }

        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: run {
            AppLogger.w(TAG, "Could not extract sender from notification")
            return
        }
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: run {
            AppLogger.w(TAG, "Could not extract message text from notification")
            return
        }

        AppLogger.d(TAG, "Messenger notification — sender=$sender | message=$message")

        if (!message.contains(KEYWORD, ignoreCase = true)) {
            AppLogger.d(TAG, "Keyword '$KEYWORD' not matched, ignoring")
            return
        }

        AppLogger.i(TAG, "Keyword matched — checking if Messenger is enabled in Supported Apps")

        serviceScope.launch {
            if (!supportedAppsRepository.isAppEnabled(MESSENGER_PACKAGE)) {
                AppLogger.d(TAG, "Messenger is disabled in Supported Apps — reply suppressed")
                return@launch
            }
            AppLogger.i(TAG, "Messenger is enabled — preparing direct reply to $sender")
            val sent = sendDirectReply(sbn)
            if (sent) {
                replyNotificationsRepository.insert(
                    ReplyNotificationEntity(
                        appPackage = MESSENGER_PACKAGE,
                        senderName = sender,
                        replyText = REPLY_TEXT,
                    )
                )
                AppLogger.d(TAG, "Reply logged to history")
            }
        }
    }

    private fun sendDirectReply(sbn: StatusBarNotification): Boolean {
        val replyText = REPLY_TEXT
        val actions = sbn.notification.actions
        if (actions.isNullOrEmpty()) {
            AppLogger.w(TAG, "No actions found on notification — cannot reply")
            return false
        }

        // Prefer the action explicitly marked as Reply (API 28+); fall back to any action with RemoteInput
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
            "Using reply action '${replyAction.title}' | RemoteInput key=${remoteInputs[0].resultKey}"
        )

        val replyIntent = Intent()
        val bundle = Bundle()
        remoteInputs.forEach { ri ->
            bundle.putCharSequence(ri.resultKey, replyText)
        }
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
