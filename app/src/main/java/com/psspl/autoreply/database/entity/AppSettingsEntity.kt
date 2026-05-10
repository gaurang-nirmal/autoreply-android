package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Single-row table — id is always 1
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "is_auto_reply_enabled")
    val isAutoReplyEnabled: Boolean = false,

    // Free plan cap; will be driven by subscription in future
    @ColumnInfo(name = "max_active_apps")
    val maxActiveApps: Int = 2,

    @ColumnInfo(name = "notifications_last_viewed_at")
    val notificationsLastViewedAt: Long = 0L,

    @ColumnInfo(name = "app_lock_enabled")
    val appLockEnabled: Boolean = false,

    @ColumnInfo(name = "theme_mode")
    val themeMode: String = "SYSTEM",

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
