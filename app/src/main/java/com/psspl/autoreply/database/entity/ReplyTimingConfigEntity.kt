package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores reply-timing + reply-limit configuration for one reply module.
 * [replyType] is the PK — one row per module (e.g. "keyword", "menu").
 */
@Entity(tableName = "reply_timing_config")
data class ReplyTimingConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "reply_type")
    val replyType: String,

    /** One of the ReplyMode enum names */
    @ColumnInfo(name = "reply_mode")
    val replyMode: String = "EVERY_TIME",

    /** Duration in seconds used by REPLY_AND_WAIT mode */
    @ColumnInfo(name = "wait_seconds")
    val waitSeconds: Int = 60,

    /** Duration in seconds used by REPLY_AFTER_DELAY mode */
    @ColumnInfo(name = "delay_seconds")
    val delaySeconds: Int = 5,

    @ColumnInfo(name = "reply_limit_enabled")
    val replyLimitEnabled: Boolean = false,

    @ColumnInfo(name = "max_replies")
    val maxReplies: Int = 50,
)
