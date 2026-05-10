package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row configuration for the Welcome Message feature.
 * Always stored with id = 1 (upsert pattern).
 */
@Entity(tableName = "welcome_message_config")
data class WelcomeMessageConfigEntity(
    @PrimaryKey val id: Int = 1,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = false,

    /** Days before a welcome message is resent to the same contact */
    @ColumnInfo(name = "cooldown_days")
    val cooldownDays: Int = 3,

    @ColumnInfo(name = "message")
    val message: String = "Welcome, thank you for contacting me, I will get back to you shortly.",
)
