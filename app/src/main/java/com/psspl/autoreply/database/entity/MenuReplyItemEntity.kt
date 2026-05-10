package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "menu_reply_items",
    foreignKeys = [
        ForeignKey(
            entity = MenuReplyEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_reply_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("menu_reply_id"),
        Index("parent_item_id"),
    ],
)
data class MenuReplyItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** FK → menu_replies.id */
    @ColumnInfo(name = "menu_reply_id")
    val menuReplyId: Int,

    /**
     * null  = top-level item (direct child of the trigger)
     * > 0   = nested item (child of another menu item)
     */
    @ColumnInfo(name = "parent_item_id")
    val parentItemId: Int? = null,

    /** 1-based display order within the same parent */
    val position: Int = 1,

    /** The text shown / sent for this menu option */
    val text: String,

    /**
     * When true, auto-reply stops after this option is chosen.
     * Configurable via "More Options" screen.
     */
    @ColumnInfo(name = "stop_reply")
    val stopReply: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
