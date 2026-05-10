package com.psspl.autoreply.ui.screens.autoreplyconfig

enum class ReplyType(val label: String, val description: String) {
    CUSTOM("Custom Message", "Write your own message to be sent as a reply."),
    KEYWORD("Keyword Reply", "Send personalized replies based on keywords."),
    SPREADSHEET("Spreadsheet Reply", "Send reply messages using spreadsheet data."),
    MENU("Menu Reply", "Send a list of options to choose from, with sub-menu replies."),
    AI_REPLY("AI Reply", "Send AI-generated replies using ChatGPT, Gemini, or DeepSeek."),
    SERVER("Server Reply", "Send real-time responses from your own server."),
}
