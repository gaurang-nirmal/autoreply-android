package com.psspl.autoreply.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psspl.autoreply.repository.SpreadsheetRepository
import com.psspl.autoreply.utils.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager [CoroutineWorker] that syncs all linked Google Spreadsheets
 * in the background. Scheduled by [com.psspl.autoreply.ui.screens.spreadsheet.SpreadsheetViewModel] when Auto Sync is enabled.
 *
 * Uses Hilt's [HiltWorker] + [AssistedInject] pattern so the worker can
 * receive injected dependencies from the Hilt graph without needing a
 * custom [androidx.work.WorkerFactory].
 */
@HiltWorker
class SpreadsheetSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val spreadsheetRepository: SpreadsheetRepository,
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SpreadsheetSyncWorker"
        const val WORK_NAME = "spreadsheet_auto_sync"
    }

    override suspend fun doWork(): Result {
        AppLogger.i(TAG, "Auto-sync started")
        return try {
            spreadsheetRepository.syncAllSheets()
            AppLogger.i(TAG, "Auto-sync complete")
            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Auto-sync failed: ${e.message}")
            // Retry up to the WorkManager default retry limit
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
