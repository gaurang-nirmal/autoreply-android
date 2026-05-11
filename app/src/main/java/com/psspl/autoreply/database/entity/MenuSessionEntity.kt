package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_sessions")
data class MenuSessionEntity(
    /** "$appPackage:${senderName.trim().lowercase()}" */
    @PrimaryKey
    @ColumnInfo(name = "contact_key")
    val contactKey: String,

    @ColumnInfo(name = "menu_reply_id")
    val menuReplyId: Int,

    /**
     * null  = user is at the top-level menu for this trigger
     * > 0   = user is viewing children of this menu item
     */
    @ColumnInfo(name = "current_parent_item_id")
    val currentParentItemId: Int? = null,

    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
