package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_replies")
data class MenuReplyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The incoming trigger message that starts this menu flow */
    @ColumnInfo(name = "trigger_message")
    val triggerMessage: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
