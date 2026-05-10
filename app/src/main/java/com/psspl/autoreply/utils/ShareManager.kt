package com.psspl.autoreply.utils

import android.content.Context
import android.content.Intent

object ShareManager {

    fun shareAppInvite(context: Context) {
        val text = "Hey! I'm using AutoReply to send automated replies. Check it out!"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Invite a Friend").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
