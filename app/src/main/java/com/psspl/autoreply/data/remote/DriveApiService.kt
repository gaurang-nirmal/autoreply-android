package com.psspl.autoreply.data.remote

import com.psspl.autoreply.data.remote.model.DriveFileListResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Retrofit interface for the Google Drive REST API.
 * Base URL: https://www.googleapis.com/
 *
 * Used to list the user's existing Google Spreadsheets so they can pick one
 * to link in the Add Spreadsheet flow.
 */
interface DriveApiService {

    /**
     * Lists all Google Sheets files in the user's Drive, ordered by last modified.
     * Returns id and name for each file — enough to display and link them.
     */
    @GET("drive/v3/files")
    suspend fun listSpreadsheets(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String = "mimeType='application/vnd.google-apps.spreadsheet' and trashed=false",
        @Query("fields") fields: String = "files(id,name)",
        @Query("orderBy") orderBy: String = "modifiedTime desc",
        @Query("pageSize") pageSize: Int = 50,
    ): DriveFileListResponse
}
