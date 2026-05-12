package com.psspl.autoreply.data.remote.model

/** Body for POST /v4/spreadsheets/{id}/values/{range}:append — save reply logs to sheet. */
data class AppendValuesRequest(
    val range: String,
    val majorDimension: String = "ROWS",
    val values: List<List<String>>,
)
