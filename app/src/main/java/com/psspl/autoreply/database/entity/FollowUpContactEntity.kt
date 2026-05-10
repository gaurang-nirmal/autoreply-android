package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores reply message texts belonging to the Specific or Exclude filter lists
@Entity(tableName = "follow_up_contact")
data class FollowUpContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // "SPECIFIC" or "EXCLUDE"
    @ColumnInfo(name = "list_type")
    val listType: String,

    @ColumnInfo(name = "message_text")
    val messageText: String,

    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
)
