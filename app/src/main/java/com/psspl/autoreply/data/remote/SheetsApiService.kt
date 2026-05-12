package com.psspl.autoreply.data.remote

import com.psspl.autoreply.data.remote.model.AppendValuesRequest
import com.psspl.autoreply.data.remote.model.CreateSpreadsheetRequest
import com.psspl.autoreply.data.remote.model.CreateSpreadsheetResponse
import com.psspl.autoreply.data.remote.model.SheetValuesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the Google Sheets REST API.
 * Base URL: https://sheets.googleapis.com/
 *
 * Every call requires [authHeader] = "Bearer {accessToken}" obtained via
 * [com.google.android.gms.auth.api.identity.Identity.getAuthorizationClient].
 */
interface SheetsApiService {

    /**
     * Reads all rows from columns A and B of a spreadsheet.
     * Row 0 is typically the header row ("keyword", "reply_message") — skip in repository.
     */
    @GET("v4/spreadsheets/{spreadsheetId}/values/A:B")
    suspend fun getValues(
        @Header("Authorization") authHeader: String,
        @Path("spreadsheetId") spreadsheetId: String,
    ): SheetValuesResponse

    /**
     * Creates a new spreadsheet in the user's Google Drive with a pre-seeded header row.
     */
    @POST("v4/spreadsheets")
    suspend fun createSpreadsheet(
        @Header("Authorization") authHeader: String,
        @Body request: CreateSpreadsheetRequest,
    ): CreateSpreadsheetResponse

    /**
     * Appends rows to a sheet — used to save auto-reply logs back to Google Sheets.
     * [valueInputOption] = "USER_ENTERED" allows formula/date parsing; "RAW" treats everything as text.
     */
    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendValues(
        @Header("Authorization") authHeader: String,
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "RAW",
        @Body request: AppendValuesRequest,
    ): retrofit2.Response<Unit>
}
