package com.psspl.autoreply.data.remote.model

/** Response from POST /v4/spreadsheets */
data class CreateSpreadsheetResponse(
    val spreadsheetId: String,
    val properties: SpreadsheetProperties,
    val spreadsheetUrl: String = "",
)
