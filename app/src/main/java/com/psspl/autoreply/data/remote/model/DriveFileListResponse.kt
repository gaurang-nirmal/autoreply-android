package com.psspl.autoreply.data.remote.model

/** Response from GET /drive/v3/files — list of Google Sheets in user's Drive. */
data class DriveFileListResponse(
    val files: List<DriveFile> = emptyList(),
)

data class DriveFile(
    val id: String,
    val name: String,
)
