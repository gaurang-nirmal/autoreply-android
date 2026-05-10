package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.ReplyTimingDao
import com.psspl.autoreply.database.entity.ReplyLimitTrackingEntity
import com.psspl.autoreply.database.entity.ReplyTimingConfigEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplyTimingRepository @Inject constructor(
    private val dao: ReplyTimingDao,
) {

    // ─── Config ───────────────────────────────────────────────────────────────

    fun getConfig(replyType: String): Flow<ReplyTimingConfigEntity?> =
        dao.getConfig(replyType)

    suspend fun upsertConfig(config: ReplyTimingConfigEntity) =
        dao.upsertConfig(config)

    // ─── Limit tracking ───────────────────────────────────────────────────────

    fun getTrackingForType(replyType: String): Flow<List<ReplyLimitTrackingEntity>> =
        dao.getTrackingForType(replyType)

    suspend fun getTracking(replyType: String, contactKey: String): ReplyLimitTrackingEntity? =
        dao.getTracking(replyType, contactKey)

    suspend fun incrementReplyCount(replyType: String, contactKey: String) {
        val existing = dao.getTracking(replyType, contactKey)
        dao.upsertTracking(
            ReplyLimitTrackingEntity(
                replyType = replyType,
                contactKey = contactKey,
                replyCount = (existing?.replyCount ?: 0) + 1,
                lastReplyAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun clearTrackingForType(replyType: String) =
        dao.clearTrackingForType(replyType)

    suspend fun clearTrackingForContact(replyType: String, contactKey: String) =
        dao.clearTrackingForContact(replyType, contactKey)

    /**
     * Returns true if a reply is allowed for this contact under the current limit config.
     * Always returns true when [replyLimitEnabled] is false.
     */
    suspend fun isReplyAllowed(
        replyType: String,
        contactKey: String,
        replyLimitEnabled: Boolean,
        maxReplies: Int,
    ): Boolean {
        if (!replyLimitEnabled) return true
        val tracking = dao.getTracking(replyType, contactKey) ?: return true
        return tracking.replyCount < maxReplies
    }
}
