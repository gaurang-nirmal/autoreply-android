package com.psspl.autoreply.ui.screens.replynotifications

import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReplyNotificationExportFormatter {
    private val exportDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

    fun format(notifications: List<ReplyNotificationEntity>): String {
        if (notifications.isEmpty()) {
            return "No reply notifications found for the selected filters."
        }

        return buildString {
            appendLine("Reply Notification History")
            appendLine("Total: ${notifications.size}")
            appendLine()
            notifications.forEachIndexed { index, entity ->
                appendLine("${index + 1}. ${entity.senderName}")
                appendLine("App: ${ReplyNotificationsViewModel.appDisplayName(entity.appPackage)}")
                appendLine("Date: ${exportDateFormat.format(Date(entity.timestamp))}")
                appendLine("Reply: ${entity.replyText}")
                if (index != notifications.lastIndex) appendLine()
            }
        }
    }
}
