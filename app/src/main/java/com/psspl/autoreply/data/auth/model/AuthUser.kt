package com.psspl.autoreply.data.auth.model

/**
 * Domain model representing an authenticated user.
 * Populated from the backend response — not from any third-party SDK.
 *
 * Note: The Google ID token is intentionally excluded here.
 * Only the app-issued JWT (stored separately in [SessionManager]) is persisted.
 */
data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
)
