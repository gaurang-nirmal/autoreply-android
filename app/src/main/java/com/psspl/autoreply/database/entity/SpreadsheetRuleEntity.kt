package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One keyword → reply_message row synced from a Google Spreadsheet.
 *
 * The sheet is expected to have exactly two columns:
 *   Column A = keyword (incoming message trigger)
 *   Column B = reply_message (auto-reply text)
 *
 * The header row ("keyword", "reply_message") is skipped during sync.
 * Rules are cascade-deleted when the parent [SpreadsheetEntity] is removed.
 */
@Entity(
    tableName = "spreadsheet_rules",
    foreignKeys = [
        ForeignKey(
            entity = SpreadsheetEntity::class,
            parentColumns = ["id"],
            childColumns = ["spreadsheet_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("spreadsheet_id")],
)
data class SpreadsheetRuleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Foreign key → spreadsheets.id */
    @ColumnInfo(name = "spreadsheet_id")
    val spreadsheetId: String,

    /** Incoming message keyword (case-insensitive contains match). */
    val keyword: String,

    /** Auto-reply text to send when keyword matches. */
    @ColumnInfo(name = "reply_message")
    val replyMessage: String,
)
