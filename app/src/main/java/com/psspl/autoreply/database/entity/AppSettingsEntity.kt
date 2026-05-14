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

    @ColumnInfo(name = "auto_reply_message")
    val autoReplyMessage: String = "I am sleeping, text you later.",

    // CUSTOM | KEYWORD | SPREADSHEET | MENU | AI_REPLY | SERVER
    @ColumnInfo(name = "reply_type")
    val replyType: String = "CUSTOM",

    /** Whether the Messages card on Dashboard is expanded (list visible). */
    @ColumnInfo(name = "messages_expanded")
    val messagesExpanded: Boolean = true,

    // ── Spreadsheet settings ──────────────────────────────────────────────────

    /** Auto-sync all linked spreadsheets periodically via WorkManager. */
    @ColumnInfo(name = "spreadsheet_auto_sync")
    val isSpreadsheetAutoSync: Boolean = false,

    /** Interval in hours between auto-syncs (default 24 h). */
    @ColumnInfo(name = "spreadsheet_sync_interval_hours")
    val spreadsheetSyncIntervalHours: Int = 24,

    /** Automatically append each sent reply to [spreadsheetSaveSheetId]. */
    @ColumnInfo(name = "spreadsheet_auto_save")
    val isSpreadsheetAutoSave: Boolean = false,

    /** spreadsheetId of the sheet used for saving reply logs; "" = none selected. */
    @ColumnInfo(name = "spreadsheet_save_sheet_id")
    val spreadsheetSaveSheetId: String = "",

    // ── Server Reply settings ─────────────────────────────────────────────────

    @ColumnInfo(name = "server_reply_url")
    val serverReplyUrl: String = "",

    @ColumnInfo(name = "server_reply_header_name")
    val serverReplyHeaderName: String = "",

    @ColumnInfo(name = "server_reply_header_value")
    val serverReplyHeaderValue: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
