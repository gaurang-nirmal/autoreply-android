package com.psspl.autoreply.data.network.model

data class ContactRequest(
    val email: String,
    val subject: String,
    val message: String,
    val attachmentBase64: String? = null,
    val attachmentName: String? = null,
)

data class ContactResponse(
    val ticketId: String?,
    val message: String,
)

data class DeleteAccountResponse(
    val success: Boolean,
    val message: String,
)
