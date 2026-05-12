package com.psspl.autoreply.ui.screens.spreadsheet

import android.app.PendingIntent
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.psspl.autoreply.data.remote.model.DriveFile
import com.psspl.autoreply.database.entity.SpreadsheetEntity
import com.psspl.autoreply.database.entity.SpreadsheetRuleEntity
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.SpreadsheetRepository
import com.psspl.autoreply.service.SpreadsheetSyncWorker
import com.psspl.autoreply.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// ─── UI State ─────────────────────────────────────────────────────────────────

data class SpreadsheetUiState(
    val sheets: List<SpreadsheetEntity> = emptyList(),
    val isAutoSync: Boolean = false,
    val syncIntervalHours: Int = 24,
    val isAutoSave: Boolean = false,
    val saveSheetId: String = "",
    val isSyncing: Boolean = false,
    val isLoadingDriveFiles: Boolean = false,
    val driveFiles: List<DriveFile> = emptyList(),
    val snackbarMessage: String? = null,
)

// ─── Google Auth Event ────────────────────────────────────────────────────────

sealed class GoogleAuthEvent {
    /** User needs to tap through the consent screen — launch this PendingIntent. */
    data class NeedsResolution(val pendingIntent: PendingIntent) : GoogleAuthEvent()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class SpreadsheetViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spreadsheetRepository: SpreadsheetRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "SpreadsheetVM"
        private const val SCOPE_SPREADSHEETS = "https://www.googleapis.com/auth/spreadsheets"
        private const val SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file"
        private const val SCOPE_DRIVE_READONLY = "https://www.googleapis.com/auth/drive.readonly"
    }

    private val _uiState = MutableStateFlow(SpreadsheetUiState())
    val uiState: StateFlow<SpreadsheetUiState> = _uiState.asStateFlow()

    /** One-shot auth events — the UI observes and launches the PendingIntent. */
    private val _authEvent = MutableSharedFlow<GoogleAuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    val isAutoSync: StateFlow<Boolean> = appSettingsRepository.isSpreadsheetAutoSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isAutoSave: StateFlow<Boolean> = appSettingsRepository.isSpreadsheetAutoSave
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val saveSheetId: StateFlow<String> = appSettingsRepository.spreadsheetSaveSheetId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val syncIntervalHours: StateFlow<Int> = appSettingsRepository.spreadsheetSyncIntervalHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 24)

    val sheets: StateFlow<List<SpreadsheetEntity>> = spreadsheetRepository.getAllSheets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getRulesForSheet(spreadsheetId: String): StateFlow<List<SpreadsheetRuleEntity>> =
        spreadsheetRepository.getRulesForSheet(spreadsheetId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Authorization ─────────────────────────────────────────────────────────

    /**
     * Requests Google OAuth2 authorization for Sheets + Drive scopes.
     *
     * - If the app is already authorized (valid token stored), calls [onAuthorized] directly.
     * - If silent auth succeeds, stores the token and calls [onAuthorized].
     * - If user consent is required, emits a [GoogleAuthEvent.NeedsResolution] — the UI must
     *   launch the [PendingIntent] and call [onAuthorizationResult] with the result [Intent].
     *
     * @param onAuthorized callback invoked when a valid token is available.
     */
    fun requestGoogleAuthorization(onAuthorized: () -> Unit) {
        viewModelScope.launch {
            // Already have a valid token — proceed immediately
            if (spreadsheetRepository.isAuthorized()) {
                onAuthorized()
                return@launch
            }

            try {
                val authRequest = AuthorizationRequest.builder()
                    .setRequestedScopes(
                        listOf(
                            Scope(SCOPE_SPREADSHEETS),
                            Scope(SCOPE_DRIVE_FILE),
                            Scope(SCOPE_DRIVE_READONLY),
                        )
                    )
                    .build()

                val authResult = suspendCancellableCoroutine { continuation ->
                    Identity.getAuthorizationClient(context)
                        .authorize(authRequest)
                        .addOnSuccessListener { continuation.resume(it) }
                        .addOnFailureListener { continuation.resumeWithException(it) }
                }

                if (authResult.hasResolution()) {
                    // User needs to grant consent — emit the PendingIntent to the UI
                    val pi = authResult.pendingIntent ?: return@launch
                    _authEvent.emit(GoogleAuthEvent.NeedsResolution(pi))
                } else {
                    // Silent success — token is immediately available
                    val token = authResult.accessToken ?: return@launch
                    val expiry = System.currentTimeMillis() + 3_600_000L // 1 hour
                    spreadsheetRepository.saveToken(token, expiry)
                    AppLogger.i(TAG, "Google auth: silent success, token stored")
                    onAuthorized()
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Google auth failed: ${e.message}")
                showSnackbar("Google authorization failed. Please try again.")
            }
        }
    }

    /**
     * Called by the UI after the user completes the consent screen (ActivityResult).
     * Extracts the access token from the Intent and stores it.
     *
     * @param intent The [android.content.Intent] from [ActivityResult.data]; null = user cancelled.
     * @param onAuthorized callback invoked when the token has been stored successfully.
     */
    fun onAuthorizationResult(intent: android.content.Intent?, onAuthorized: () -> Unit) {
        if (intent == null) {
            showSnackbar("Authorization cancelled.")
            return
        }
        viewModelScope.launch {
            try {
                val authResult = Identity.getAuthorizationClient(context)
                    .getAuthorizationResultFromIntent(intent)
                val token = authResult.accessToken ?: run {
                    showSnackbar("Could not retrieve access token.")
                    return@launch
                }
                val expiry = System.currentTimeMillis() + 3_600_000L
                spreadsheetRepository.saveToken(token, expiry)
                AppLogger.i(TAG, "Google auth: user consent granted, token stored")
                onAuthorized()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Authorization result parsing failed: ${e.message}")
                showSnackbar("Authorization failed. Please try again.")
            }
        }
    }

    // ── Drive file listing ────────────────────────────────────────────────────

    fun loadDriveFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDriveFiles = true, driveFiles = emptyList()) }
            spreadsheetRepository.listDriveSheets()
                .onSuccess { files ->
                    _uiState.update { it.copy(driveFiles = files, isLoadingDriveFiles = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingDriveFiles = false) }
                    showSnackbar("Failed to load Drive files. Check your connection.")
                }
        }
    }

    // ── Sheet operations ──────────────────────────────────────────────────────

    fun linkSheet(id: String, name: String) {
        viewModelScope.launch {
            spreadsheetRepository.linkSheet(id, name)
            // Auto-sync immediately so rules are ready without any manual step
            _uiState.update { it.copy(isSyncing = true) }
            spreadsheetRepository.syncSheet(id)
                .onSuccess { count ->
                    showSnackbar("\"$name\" linked and synced — $count rules loaded.")
                }
                .onFailure {
                    showSnackbar("\"$name\" linked. Sync failed — tap Sync Now to retry.")
                }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun deleteSheet(spreadsheetId: String, name: String) {
        viewModelScope.launch {
            spreadsheetRepository.deleteSheet(spreadsheetId)
            // If this was the auto-save sheet, clear the setting
            if (saveSheetId.value == spreadsheetId) {
                appSettingsRepository.setSpreadsheetSaveSheetId("")
            }
            showSnackbar("\"$name\" removed.")
        }
    }

    fun syncSheet(spreadsheetId: String, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            spreadsheetRepository.syncSheet(spreadsheetId)
                .onSuccess { count -> showSnackbar("\"$name\" synced — $count rules loaded.") }
                .onFailure { showSnackbar("Sync failed. Check your connection.") }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    fun syncAllSheets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            spreadsheetRepository.syncAllSheets()
            _uiState.update { it.copy(isSyncing = false) }
            showSnackbar("All spreadsheets synced.")
        }
    }

    // ── Create new spreadsheet ────────────────────────────────────────────────

    fun createSpreadsheet(name: String, onSuccess: (spreadsheetId: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            spreadsheetRepository.createSpreadsheet(name.trim())
                .onSuccess { response ->
                    _uiState.update { it.copy(isSyncing = false) }
                    onSuccess(response.spreadsheetId)
                }
                .onFailure {
                    _uiState.update { it.copy(isSyncing = false) }
                    showSnackbar("Failed to create spreadsheet. Please try again.")
                }
        }
    }

    // ── Auto Sync settings ────────────────────────────────────────────────────

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setSpreadsheetAutoSync(enabled)
            if (enabled) scheduleAutoSync() else cancelAutoSync()
        }
    }

    fun setSyncInterval(hours: Int) {
        viewModelScope.launch {
            appSettingsRepository.setSpreadsheetSyncIntervalHours(hours)
            if (isAutoSync.value) scheduleAutoSync() // re-schedule with new interval
        }
    }

    // ── Auto Save settings ────────────────────────────────────────────────────

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setSpreadsheetAutoSave(enabled)
        }
    }

    fun setSaveSheetId(sheetId: String) {
        viewModelScope.launch {
            appSettingsRepository.setSpreadsheetSaveSheetId(sheetId)
        }
    }

    // ── WorkManager scheduling ────────────────────────────────────────────────

    private fun scheduleAutoSync() {
        val intervalHours = syncIntervalHours.value.toLong().coerceAtLeast(1L)
        val request = PeriodicWorkRequestBuilder<SpreadsheetSyncWorker>(
            intervalHours, TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(SpreadsheetSyncWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SpreadsheetSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        AppLogger.i(TAG, "Auto-sync scheduled every ${intervalHours}h")
    }

    private fun cancelAutoSync() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SpreadsheetSyncWorker.WORK_NAME)
        AppLogger.i(TAG, "Auto-sync cancelled")
    }

    // ── Snackbar ──────────────────────────────────────────────────────────────

    private fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
