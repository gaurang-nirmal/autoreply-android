package com.psspl.autoreply.utils

enum class ReplyOption(val displayName: String) {
    MENU_REPLY("Menu Reply"),
    CHATGPT("ChatGPT"),
    GEMINI("Gemini"),
    SERVER_REPLY("Server Reply"),
    DO_NOT_REPLY("Do Not Reply"),
}

enum class MatchType(val displayName: String) {
    EXACT("Exact Match"),
    CONTAINS("Contains"),
}

/** All substitution tags users can insert into a reply message. */
val REPLY_TAGS = listOf("name", "first name", "last name", "date", "time", "message")

/** Convert comma-separated DB string ↔ Set<ReplyOption>. */
fun String.toReplyOptionSet(): Set<ReplyOption> =
    splitToSequence(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { name -> ReplyOption.entries.find { it.name == name } }
        .toSet()

fun Set<ReplyOption>.toDbString(): String = joinToString(",") { it.name }
