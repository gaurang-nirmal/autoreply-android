package com.psspl.autoreply.engine

import com.psspl.autoreply.database.dao.MenuReplyDao
import com.psspl.autoreply.database.dao.MenuSessionDao
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import com.psspl.autoreply.database.entity.MenuSessionEntity
import com.psspl.autoreply.engine.MenuReplyEngine.Companion.SESSION_TIMEOUT_MS
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drives the Menu Reply feature at runtime.
 *
 * For each incoming message it either:
 *  - matches a trigger and starts a new menu session (top-level items sent),
 *  - advances an existing session (sub-menu or leaf resolution), or
 *  - returns [MenuEngineResult.NoReply] when the message is irrelevant.
 *
 * Sessions expire after [SESSION_TIMEOUT_MS] of inactivity so a contact that
 * abandons a flow is not stuck mid-menu indefinitely.
 */
@Singleton
class MenuReplyEngine @Inject constructor(
    private val menuReplyDao: MenuReplyDao,
    private val menuSessionDao: MenuSessionDao,
) {

    companion object {
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1_000L // 30 minutes
    }

    /**
     * Process an incoming [message] from [sender] identified by [contactKey].
     *
     * Returns a [MenuEngineResult.Reply] (the text to send) or [MenuEngineResult.NoReply].
     * [MenuEngineResult.Reply.isNewSession] is true only for the initial trigger match so the
     * caller can selectively apply timing gates (navigation replies must not be gated).
     */
    suspend fun handle(
        contactKey: String,
        sender: String,
        message: String,
    ): MenuEngineResult {
        // Prune stale sessions before checking
        menuSessionDao.deleteExpiredSessions(System.currentTimeMillis() - SESSION_TIMEOUT_MS)

        val session = menuSessionDao.getSession(contactKey)

        return if (session != null) {
            handleSessionReply(session, contactKey, message)
        } else {
            handleTriggerMatch(contactKey, sender, message)
        }
    }

    // ─── Trigger matching ─────────────────────────────────────────────────────

    private suspend fun handleTriggerMatch(
        contactKey: String,
        sender: String,
        message: String,
    ): MenuEngineResult {
        val triggers = menuReplyDao.getActiveRepliesOnce()
        val matched = triggers.firstOrNull { trigger ->
            message.trim().contains(trigger.triggerMessage.trim(), ignoreCase = true)
        } ?: return MenuEngineResult.NoReply

        val items = menuReplyDao.getTopLevelItemsOnce(matched.id)
        if (items.isEmpty()) return MenuEngineResult.NoReply

        menuSessionDao.upsertSession(
            MenuSessionEntity(
                contactKey = contactKey,
                menuReplyId = matched.id,
                currentParentItemId = null,
            )
        )

        val firstName = sender.trim().split("\\s+".toRegex()).firstOrNull() ?: sender
        return MenuEngineResult.Reply(
            text = buildMenuMessage("Hello $firstName! Here are the available options:", items),
            isNewSession = true,
        )
    }

    // ─── Session navigation ───────────────────────────────────────────────────

    private suspend fun handleSessionReply(
        session: MenuSessionEntity,
        contactKey: String,
        message: String,
    ): MenuEngineResult {
        val items = if (session.currentParentItemId == null) {
            menuReplyDao.getTopLevelItemsOnce(session.menuReplyId)
        } else {
            menuReplyDao.getItemsByParentOnce(session.currentParentItemId)
        }

        if (items.isEmpty()) {
            menuSessionDao.deleteSession(contactKey)
            return MenuEngineResult.NoReply
        }

        val trimmed = message.trim()

        // Accept a numeric selection ("1", "2", …) or the full item text
        val selected = trimmed.toIntOrNull()
            ?.let { num -> items.getOrNull(num - 1) }
            ?: items.firstOrNull { it.text.trim().equals(trimmed, ignoreCase = true) }

        if (selected == null) {
            // Invalid input — re-send the current menu level with a prompt
            val reprompt =
                "Please reply with a valid number (1–${items.size}):\n\n${buildMenuList(items)}"
            return MenuEngineResult.Reply(reprompt, isNewSession = false)
        }

        val children = menuReplyDao.getItemsByParentOnce(selected.id)

        return if (children.isNotEmpty()) {
            // Navigate one level deeper
            menuSessionDao.upsertSession(
                session.copy(
                    currentParentItemId = selected.id,
                    updatedAt = System.currentTimeMillis(),
                )
            )
            MenuEngineResult.Reply(
                text = buildMenuMessage("${selected.text}:", children),
                isNewSession = false,
            )
        } else {
            // Leaf item — end the session
            menuSessionDao.deleteSession(contactKey)
            if (selected.stopReply) {
                // stopReply=true: bot goes completely silent after this choice
                MenuEngineResult.NoReply
            } else {
                // Echo the item text as the final response
                MenuEngineResult.Reply(selected.text, isNewSession = false)
            }
        }
    }

    // ─── Formatting helpers ───────────────────────────────────────────────────

    private fun buildMenuMessage(header: String, items: List<MenuReplyItemEntity>): String =
        "$header\n\n${buildMenuList(items)}"

    private fun buildMenuList(items: List<MenuReplyItemEntity>): String =
        items.mapIndexed { i, item -> "${i + 1}. ${item.text}" }.joinToString("\n")
}

// ─── Result type ──────────────────────────────────────────────────────────────

sealed class MenuEngineResult {
    /** Send [text] to the contact. [isNewSession] = true only for the initial trigger reply. */
    data class Reply(val text: String, val isNewSession: Boolean) : MenuEngineResult()

    /** Do not send any reply (no trigger matched, stopReply=true leaf, or empty menu). */
    object NoReply : MenuEngineResult()
}
