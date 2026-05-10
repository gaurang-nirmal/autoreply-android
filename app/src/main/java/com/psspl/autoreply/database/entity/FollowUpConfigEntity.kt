package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Single-row table — id is always 1
@Entity(tableName = "follow_up_config")
data class FollowUpConfigEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = false,

    @ColumnInfo(name = "message")
    val message: String = "Hi there! Following up on my last message.",

    // ALL | SPECIFIC | EXCLUDE
    @ColumnInfo(name = "scope")
    val scope: String = "ALL",

    @ColumnInfo(name = "delay_minutes")
    val delayMinutes: Int = 60,
)
