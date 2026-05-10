package com.psspl.autoreply.ui.screens.replytiming

enum class ReplyMode(val label: String, val description: String) {
    EVERY_TIME(
        label = "Reply every time",
        description = "Send a reply every time a new message is received.",
    ),
    REPLY_AND_WAIT(
        label = "Reply and wait",
        description = "Send a reply, then pause until the wait time is over.",
    ),
    REPLY_AFTER_DELAY(
        label = "Reply after a delay",
        description = "Send a reply after a time delay.",
    ),
    REPLY_ONCE(
        label = "Reply once",
        description = "Send reply once. To send again, restart auto-reply.",
    ),
}
