package com.psspl.autoreply.utils

import android.content.Context
import android.content.Intent

object ShareManager {

    fun shareAppInvite(context: Context) {
        val text = "Hey! I'm using AutoReply to send automated replies. Check it out!"
        shareText(context, text, "Invite a Friend")
    }

    fun shareReplyNotificationHistory(context: Context, text: String) {
        shareText(context, text, "Export Reply Notifications")
    }

    fun shareStatisticsCsv(context: Context, csv: String) {
        shareText(context, csv, "Export Statistics")
    }

    private fun shareText(
        context: Context,
        text: String,
        chooserTitle: String,
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
