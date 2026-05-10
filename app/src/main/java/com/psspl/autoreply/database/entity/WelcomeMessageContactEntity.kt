package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Tracks when the welcome message was last sent to a specific contact on a specific app.
 * Composite primary key: (app_package, contact_key).
 */
@Entity(
    tableName = "welcome_message_contacts",
    primaryKeys = ["app_package", "contact_key"],
)
data class WelcomeMessageContactEntity(
    /** e.g. "com.whatsapp" */
    @ColumnInfo(name = "app_package")
    val appPackage: String,

    /** Normalized contact identifier (phone number, username, etc.) */
    @ColumnInfo(name = "contact_key")
    val contactKey: String,

    /** Epoch millis of the last welcome message sent */
    @ColumnInfo(name = "last_sent_at")
    val lastSentAt: Long,
)
