package com.psspl.autoreply.repository

import com.psspl.autoreply.data.local.SessionManager
import com.psspl.autoreply.data.remote.DriveApiService
import com.psspl.autoreply.data.remote.SheetsApiService
import com.psspl.autoreply.data.remote.model.AppendValuesRequest
import com.psspl.autoreply.data.remote.model.CellData
import com.psspl.autoreply.data.remote.model.CreateSpreadsheetRequest
import com.psspl.autoreply.data.remote.model.CreateSpreadsheetResponse
import com.psspl.autoreply.data.remote.model.DriveFile
import com.psspl.autoreply.data.remote.model.ExtendedValue
import com.psspl.autoreply.data.remote.model.GridData
import com.psspl.autoreply.data.remote.model.RowData
import com.psspl.autoreply.data.remote.model.SheetConfig
import com.psspl.autoreply.data.remote.model.SpreadsheetProperties
import com.psspl.autoreply.database.dao.SpreadsheetDao
import com.psspl.autoreply.database.entity.SpreadsheetEntity
import com.psspl.autoreply.database.entity.SpreadsheetRuleEntity
import com.psspl.autoreply.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpreadsheetRepository @Inject constructor(
    private val spreadsheetDao: SpreadsheetDao,
    private val sheetsApiService: SheetsApiService,
    private val driveApiService: DriveApiService,
    private val sessionManager: SessionManager,
) {
    companion object {
        private const val TAG = "SpreadsheetRepo"
        private const val HEADER_KEYWORD = "keyword"
        private const val HEADER_REPLY = "reply_message"
        private const val REPLY_LOG_RANGE = "Reply_Logs!A:E"
    }

    // ── Local DB observations ─────────────────────────────────────────────────

    fun getAllSheets(): Flow<List<SpreadsheetEntity>> = spreadsheetDao.getAllSheets()

    fun getRulesForSheet(spreadsheetId: String): Flow<List<SpreadsheetRuleEntity>> =
        spreadsheetDao.getRulesForSheet(spreadsheetId)

    fun searchRulesForSheet(
        spreadsheetId: String,
        query: String
    ): Flow<List<SpreadsheetRuleEntity>> =
        spreadsheetDao.searchRulesForSheet(spreadsheetId, query)

    /** One-shot read of all rules — used by the notification service for fast keyword matching. */
    suspend fun getAllRules(): List<SpreadsheetRuleEntity> = spreadsheetDao.getAllRules()

    // ── Sheet management ──────────────────────────────────────────────────────

    suspend fun linkSheet(id: String, name: String) {
        spreadsheetDao.insertSheet(SpreadsheetEntity(id = id, name = name))
    }

    suspend fun deleteSheet(id: String) {
        spreadsheetDao.deleteSheetById(id)
    }

    // ── Drive: list user's existing spreadsheets ──────────────────────────────

    /**
     * Fetches the list of Google Sheets from the user's Drive.
     * Returns an empty list on failure (network error / token expired).
     */
    suspend fun listDriveSheets(): Result<List<DriveFile>> = runCatching {
        val token = requireToken()
        driveApiService.listSpreadsheets(authHeader = "Bearer $token").files
    }.onFailure { AppLogger.e(TAG, "listDriveSheets failed: ${it.message}") }

    // ── Sync: fetch rules from Google Sheets and cache locally ───────────────

    /**
     * Syncs a single spreadsheet: fetches rows A:B, skips the header row,
     * replaces all locally-cached rules for that sheet, then updates the
     * last-sync timestamp.
     *
     * @return Number of rules imported (0 on empty sheet, -1 on error).
     */
    suspend fun syncSheet(spreadsheetId: String): Result<Int> = runCatching {
        val token = requireToken()
        val response = sheetsApiService.getValues(
            authHeader = "Bearer $token",
            spreadsheetId = spreadsheetId,
        )

        val rows = response.values.orEmpty()
        AppLogger.d(TAG, "Sync $spreadsheetId — ${rows.size} rows fetched")

        // Build rule entities, skipping the header row and any malformed rows
        val rules = rows
            .drop(1) // skip header row ("keyword", "reply_message")
            .mapNotNull { row ->
                if (row.size < 2) return@mapNotNull null
                val keyword = row[0].trim()
                val reply = row[1].trim()
                if (keyword.isEmpty() || reply.isEmpty()) return@mapNotNull null
                SpreadsheetRuleEntity(
                    spreadsheetId = spreadsheetId,
                    keyword = keyword,
                    replyMessage = reply,
                )
            }

        // Atomic replace: delete old → insert new
        spreadsheetDao.deleteRulesForSheet(spreadsheetId)
        if (rules.isNotEmpty()) spreadsheetDao.insertRules(rules)
        spreadsheetDao.updateLastSyncAt(spreadsheetId, System.currentTimeMillis())

        AppLogger.i(TAG, "Sync complete for $spreadsheetId — ${rules.size} rules cached")
        rules.size
    }.onFailure { AppLogger.e(TAG, "syncSheet($spreadsheetId) failed: ${it.message}") }

    /** Syncs all linked spreadsheets sequentially. */
    suspend fun syncAllSheets() {
        // .first() takes one snapshot from the Room Flow and completes immediately —
        // unlike .collect{} which would hang forever waiting for more emissions.
        val sheets = spreadsheetDao.getAllSheets().first()
        sheets.forEach { sheet -> syncSheet(sheet.id) }
    }

    // ── Create new spreadsheet ────────────────────────────────────────────────

    /**
     * Creates a new Google Spreadsheet with the given name and a pre-seeded
     * header row, then links it in the local DB.
     *
     * @return The new spreadsheet's id and url on success.
     */
    suspend fun createSpreadsheet(name: String): Result<CreateSpreadsheetResponse> = runCatching {
        val token = requireToken()

        // Pre-seed the sheet with the header row so the user knows the expected format
        val headerRow = RowData(
            values = listOf(
                CellData(ExtendedValue(HEADER_KEYWORD)),
                CellData(ExtendedValue(HEADER_REPLY)),
            )
        )
        val request = CreateSpreadsheetRequest(
            properties = SpreadsheetProperties(title = name),
            sheets = listOf(
                SheetConfig(
                    data = listOf(GridData(rowData = listOf(headerRow)))
                )
            ),
        )

        val response = sheetsApiService.createSpreadsheet(
            authHeader = "Bearer $token",
            request = request,
        )

        // Auto-link the newly created sheet
        spreadsheetDao.insertSheet(
            SpreadsheetEntity(id = response.spreadsheetId, name = name)
        )

        AppLogger.i(TAG, "Created spreadsheet '${response.spreadsheetId}' name='$name'")
        response
    }.onFailure { AppLogger.e(TAG, "createSpreadsheet('$name') failed: ${it.message}") }

    // ── Save reply log to Google Sheets ───────────────────────────────────────

    /**
     * Appends one row to the reply-log sheet.
     * Columns: Timestamp | App | Sender | Received Message | Sent Reply
     */
    suspend fun saveReplyToSheet(
        spreadsheetId: String,
        appPackage: String,
        sender: String,
        receivedMessage: String,
        replyText: String,
    ) = runCatching {
        val token = requireToken()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val row = listOf(timestamp, appPackage, sender, receivedMessage, replyText)
        val request = AppendValuesRequest(
            range = "Sheet1!A:E",
            values = listOf(row),
        )

        sheetsApiService.appendValues(
            authHeader = "Bearer $token",
            spreadsheetId = spreadsheetId,
            range = "Sheet1!A:E",
            request = request,
        )
        AppLogger.d(TAG, "Reply log appended to $spreadsheetId")
    }.onFailure { AppLogger.e(TAG, "saveReplyToSheet failed: ${it.message}") }

    // ── Auth helpers ──────────────────────────────────────────────────────────

    /**
     * Returns the stored Google access token.
     * Throws [IllegalStateException] if no token is stored — caller should
     * trigger the OAuth authorization flow in that case.
     */
    suspend fun requireToken(): String =
        sessionManager.getGoogleAccessToken()
            ?: error("No Google access token — authorization required")

    /** True when the stored token exists and has not yet expired. */
    suspend fun isAuthorized(): Boolean = sessionManager.isGoogleTokenValid()

    suspend fun saveToken(token: String, expiryMs: Long) =
        sessionManager.saveGoogleAccessToken(token, expiryMs)
}
