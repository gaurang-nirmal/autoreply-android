package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.SpreadsheetEntity
import com.psspl.autoreply.database.entity.SpreadsheetRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpreadsheetDao {

    // ── Spreadsheets ─────────────────────────────────────────────────────────

    @Query("SELECT * FROM spreadsheets ORDER BY added_at DESC")
    fun getAllSheets(): Flow<List<SpreadsheetEntity>>

    @Query("SELECT * FROM spreadsheets WHERE id = :id")
    suspend fun getSheetById(id: String): SpreadsheetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: SpreadsheetEntity)

    @Delete
    suspend fun deleteSheet(sheet: SpreadsheetEntity)

    @Query("DELETE FROM spreadsheets WHERE id = :id")
    suspend fun deleteSheetById(id: String)

    @Query("UPDATE spreadsheets SET last_sync_at = :timestamp WHERE id = :id")
    suspend fun updateLastSyncAt(id: String, timestamp: Long)

    // ── Spreadsheet Rules ─────────────────────────────────────────────────────

    /** Observe rules for a specific sheet (used in ViewSpreadsheetScreen). */
    @Query("SELECT * FROM spreadsheet_rules WHERE spreadsheet_id = :spreadsheetId ORDER BY id ASC")
    fun getRulesForSheet(spreadsheetId: String): Flow<List<SpreadsheetRuleEntity>>

    /** One-shot: all rules across all sheets (used by the notification service for matching). */
    @Query("SELECT * FROM spreadsheet_rules ORDER BY spreadsheet_id, id ASC")
    suspend fun getAllRules(): List<SpreadsheetRuleEntity>

    /** One-shot: rules for a specific sheet (used for search in ViewSpreadsheetScreen). */
    @Query(
        "SELECT * FROM spreadsheet_rules WHERE spreadsheet_id = :spreadsheetId " +
                "AND (keyword LIKE '%' || :query || '%' OR reply_message LIKE '%' || :query || '%') " +
                "ORDER BY id ASC"
    )
    fun searchRulesForSheet(spreadsheetId: String, query: String): Flow<List<SpreadsheetRuleEntity>>

    /** Replace all rules for a sheet — called after each sync. */
    @Query("DELETE FROM spreadsheet_rules WHERE spreadsheet_id = :spreadsheetId")
    suspend fun deleteRulesForSheet(spreadsheetId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<SpreadsheetRuleEntity>)

    /** Total rule count across all sheets (useful for stats). */
    @Query("SELECT COUNT(*) FROM spreadsheet_rules")
    fun getTotalRuleCount(): Flow<Int>
}
