package com.psspl.autoreply.data.remote.model

/**
 * Body for POST /v4/spreadsheets — creates a new Google Spreadsheet
 * with a pre-populated header row (keyword | reply_message).
 */
data class CreateSpreadsheetRequest(
    val properties: SpreadsheetProperties,
    val sheets: List<SheetConfig>? = null,
)

data class SpreadsheetProperties(val title: String)

data class SheetConfig(
    val properties: SheetTabProperties = SheetTabProperties(),
    val data: List<GridData>? = null,
)

data class SheetTabProperties(val title: String = "Sheet1")

data class GridData(val rowData: List<RowData>)

data class RowData(val values: List<CellData>)

data class CellData(val userEnteredValue: ExtendedValue)

data class ExtendedValue(val stringValue: String)
