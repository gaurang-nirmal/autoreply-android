package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Tracks how many replies have been sent to a specific contact for a specific module.
 * Composite PK: (reply_type, contact_key).
 */
@Entity(
    tableName = "reply_limit_tracking",
    primaryKeys = ["reply_type", "contact_key"],
)
data class ReplyLimitTrackingEntity(
    @ColumnInfo(name = "reply_type")
    val replyType: String,

    /** Normalised contact identifier (phone number, username, etc.) */
    @ColumnInfo(name = "contact_key")
    val contactKey: String,

    @ColumnInfo(name = "reply_count")
    val replyCount: Int = 0,

    @ColumnInfo(name = "last_reply_at")
    val lastReplyAt: Long = System.currentTimeMillis(),
)
