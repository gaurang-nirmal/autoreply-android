package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.AppSettingsDao
import com.psspl.autoreply.database.entity.AppSettingsEntity
import com.psspl.autoreply.utils.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepository @Inject constructor(
    private val dao: AppSettingsDao
) {
    val settings: Flow<AppSettingsEntity?> = dao.observe()

    val isAutoReplyEnabled: Flow<Boolean> = dao.observe().map { it?.isAutoReplyEnabled ?: false }

    val appLockEnabled: Flow<Boolean> = dao.observe().map { it?.appLockEnabled ?: false }

    val themeMode: Flow<ThemeMode> = dao.observe().map { entity ->
        ThemeMode.entries.find { it.name == entity?.themeMode } ?: ThemeMode.SYSTEM
    }

    suspend fun get(): AppSettingsEntity? = dao.get()

    suspend fun save(settings: AppSettingsEntity) = dao.insert(settings)

    suspend fun update(settings: AppSettingsEntity) = dao.update(settings)

    suspend fun markNotificationsViewed() {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(notificationsLastViewedAt = System.currentTimeMillis()))
    }

    suspend fun setAutoReplyEnabled(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(isAutoReplyEnabled = enabled))
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(appLockEnabled = enabled))
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(themeMode = mode.name))
    }

    suspend fun setAutoReplyMessage(message: String) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(autoReplyMessage = message))
    }

    suspend fun setReplyType(type: String) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(replyType = type))
    }

    suspend fun setMessagesExpanded(expanded: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(messagesExpanded = expanded))
    }

    // ── Spreadsheet settings ──────────────────────────────────────────────────

    val isSpreadsheetAutoSync: Flow<Boolean> =
        dao.observe().map { it?.isSpreadsheetAutoSync ?: false }

    val isSpreadsheetAutoSave: Flow<Boolean> =
        dao.observe().map { it?.isSpreadsheetAutoSave ?: false }

    val spreadsheetSaveSheetId: Flow<String> =
        dao.observe().map { it?.spreadsheetSaveSheetId ?: "" }

    val spreadsheetSyncIntervalHours: Flow<Int> =
        dao.observe().map { it?.spreadsheetSyncIntervalHours ?: 24 }

    suspend fun setSpreadsheetAutoSync(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(isSpreadsheetAutoSync = enabled))
    }

    suspend fun setSpreadsheetAutoSave(enabled: Boolean) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(isSpreadsheetAutoSave = enabled))
    }

    suspend fun setSpreadsheetSaveSheetId(sheetId: String) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(spreadsheetSaveSheetId = sheetId))
    }

    suspend fun setSpreadsheetSyncIntervalHours(hours: Int) {
        val current = dao.get() ?: AppSettingsEntity()
        dao.insert(current.copy(spreadsheetSyncIntervalHours = hours))
    }
}
