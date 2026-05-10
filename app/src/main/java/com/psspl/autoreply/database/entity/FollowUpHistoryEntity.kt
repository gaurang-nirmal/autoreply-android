package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follow_up_history")
data class FollowUpHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "contact_key")
    val contactKey: String,

    @ColumnInfo(name = "contact_name")
    val contactName: String = "",

    @ColumnInfo(name = "app_package")
    val appPackage: String = "",

    @ColumnInfo(name = "original_message")
    val originalMessage: String = "",

    @ColumnInfo(name = "follow_up_message")
    val followUpMessage: String,

    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Long = System.currentTimeMillis(),

    // 0 means not yet sent
    @ColumnInfo(name = "sent_at")
    val sentAt: Long = 0L,

    // PENDING | SENT | CANCELLED
    @ColumnInfo(name = "status")
    val status: String = "PENDING",
)
