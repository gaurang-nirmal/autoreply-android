package com.psspl.autoreply.data.auth.model

/**
 * Domain model representing an authenticated user.
 * Decoupled from Firebase's [com.google.firebase.auth.FirebaseUser].
 */
data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    /** The raw Google ID token used for server-side verification (if needed). */
    val idToken: String?,
)
