package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a linked Google Spreadsheet.
 * Each row stores the spreadsheetId, display name, and last-sync timestamp.
 * Rules fetched from the sheet are stored separately in [SpreadsheetRuleEntity].
 */
@Entity(tableName = "spreadsheets")
data class SpreadsheetEntity(

    /** Google Sheets spreadsheetId (unique, from Drive/Sheets API). */
    @PrimaryKey
    val id: String,

    /** Human-readable name shown in the UI (from spreadsheet properties). */
    val name: String,

    /** Epoch ms of the last successful sync; 0 = never synced. */
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long = 0L,

    /** Epoch ms when the user linked this sheet to the app. */
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
)
