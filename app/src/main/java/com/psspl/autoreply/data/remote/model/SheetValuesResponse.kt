package com.psspl.autoreply.data.remote.model

/**
 * Response from GET /v4/spreadsheets/{id}/values/{range}
 * Each inner list represents one row; index 0 = keyword, index 1 = reply_message.
 */
data class SheetValuesResponse(
    val range: String = "",
    val majorDimension: String = "",
    val values: List<List<String>>? = null,
)
