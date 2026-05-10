package com.psspl.autoreply.backup

import android.content.Context
import android.net.Uri
import com.psspl.autoreply.database.entity.MenuReplyEntity
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import com.psspl.autoreply.repository.MenuReplyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles export and import of all Menu Reply data (triggers + items).
 *
 * JSON format:
 * {
 *   "menuReplies": [ { "id", "triggerMessage", "isActive", "createdAt" }, … ],
 *   "menuItems":   [ { "id", "menuReplyId", "parentItemId", "position", "text", "stopReply", "createdAt" }, … ]
 * }
 *
 * Import algorithm:
 *  Phase 1 – insert replies, build oldId → newId map (skip duplicates by triggerMessage).
 *  Phase 2 – topological item insertion: repeatedly iterate the pending list and insert
 *             items whose parent is already mapped (or null). Retries until all done or
 *             a pass makes no progress (broken references skipped).
 */
@Singleton
class MenuReplyBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MenuReplyRepository,
) {

    // ─── Export ───────────────────────────────────────────────────────────────

    /**
     * Writes all Menu Reply data to [uri] as a JSON file.
     * @return true on success, false on failure.
     */
    suspend fun exportReplies(uri: Uri): Boolean {
        return try {
            val replies = repository.getAllReplies().first()
            val items = repository.getAllItems().first()

            val json = buildJson(replies, items)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ─── Import ───────────────────────────────────────────────────────────────

    /**
     * Reads Menu Reply data from [uri] and merges it with existing data.
     * Replies are deduplicated by trigger message (case-insensitive).
     *
     * @return number of menu replies imported, or -1 on failure.
     */
    suspend fun importReplies(uri: Uri): Int {
        return try {
            val jsonText = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return -1

            val root = JSONObject(jsonText)
            val repliesArray = root.getJSONArray("menuReplies")
            val itemsArray = root.getJSONArray("menuItems")

            // ── Phase 1: Import replies ────────────────────────────────────────
            // Build set of existing trigger messages for dedup
            val existingTriggers = repository.getAllReplies().first()
                .map { it.triggerMessage.trim().lowercase() }
                .toHashSet()

            val replyIdMap = mutableMapOf<Int, Int>() // oldId → newId
            var importedCount = 0

            for (i in 0 until repliesArray.length()) {
                val obj = repliesArray.getJSONObject(i)
                val oldId = obj.getInt("id")
                val trigger = obj.getString("triggerMessage")

                if (trigger.trim().lowercase() in existingTriggers) {
                    // Duplicate reply — skip entirely; no mapping means Phase 2
                    // will also skip all items belonging to this reply.
                    continue
                }

                val newId = repository.insertReply(
                    MenuReplyEntity(
                        triggerMessage = trigger,
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = System.currentTimeMillis(),
                    )
                ).toInt()
                replyIdMap[oldId] = newId
                existingTriggers.add(trigger.trim().lowercase())
                importedCount++
            }

            // ── Phase 2: Topological item insertion ───────────────────────────
            // Collect all items that belong to a reply we successfully mapped
            data class PendingItem(
                val oldId: Int,
                val oldMenuReplyId: Int,
                val oldParentId: Int?,
                val position: Int,
                val text: String,
                val stopReply: Boolean,
                val createdAt: Long,
            )

            val pending = mutableListOf<PendingItem>()
            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                val oldMenuReplyId = obj.getInt("menuReplyId")
                if (!replyIdMap.containsKey(oldMenuReplyId)) continue // orphaned — skip

                pending.add(
                    PendingItem(
                        oldId = obj.getInt("id"),
                        oldMenuReplyId = oldMenuReplyId,
                        oldParentId = if (obj.isNull("parentItemId")) null else obj.getInt("parentItemId"),
                        position = obj.getInt("position"),
                        text = obj.getString("text"),
                        stopReply = obj.optBoolean("stopReply", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    )
                )
            }

            val itemIdMap = mutableMapOf<Int, Int>() // oldId → newId
            var maxPasses = pending.size + 1          // guard against infinite loops

            while (pending.isNotEmpty() && maxPasses-- > 0) {
                val iter = pending.iterator()
                var madeProgress = false

                while (iter.hasNext()) {
                    val p = iter.next()
                    val newMenuReplyId = replyIdMap[p.oldMenuReplyId] ?: continue
                    val newParentId = if (p.oldParentId == null) {
                        null
                    } else {
                        itemIdMap[p.oldParentId]
                            ?: continue // parent not yet inserted; retry next pass
                    }

                    val newId = repository.insertItem(
                        MenuReplyItemEntity(
                            menuReplyId = newMenuReplyId,
                            parentItemId = newParentId,
                            position = p.position,
                            text = p.text,
                            stopReply = p.stopReply,
                            createdAt = System.currentTimeMillis(),
                        )
                    ).toInt()
                    itemIdMap[p.oldId] = newId
                    iter.remove()
                    madeProgress = true
                }

                if (!madeProgress) break // remaining items have unresolvable parents — skip
            }

            importedCount
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun buildJson(
        replies: List<MenuReplyEntity>,
        items: List<MenuReplyItemEntity>,
    ): String {
        val repliesArray = JSONArray()
        replies.forEach { r ->
            repliesArray.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("triggerMessage", r.triggerMessage)
                    put("isActive", r.isActive)
                    put("createdAt", r.createdAt)
                }
            )
        }

        val itemsArray = JSONArray()
        items.forEach { item ->
            itemsArray.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("menuReplyId", item.menuReplyId)
                    if (item.parentItemId == null) put("parentItemId", JSONObject.NULL)
                    else put("parentItemId", item.parentItemId)
                    put("position", item.position)
                    put("text", item.text)
                    put("stopReply", item.stopReply)
                    put("createdAt", item.createdAt)
                }
            )
        }

        return JSONObject().apply {
            put("menuReplies", repliesArray)
            put("menuItems", itemsArray)
        }.toString(2)
    }
}
