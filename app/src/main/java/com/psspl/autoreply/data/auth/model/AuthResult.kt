package com.psspl.autoreply.data.auth.model

/** Sealed result type returned by every authentication operation. */
sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult()

    /** User dismissed the account picker without selecting an account. */
    data object Cancelled : AuthResult()

    /**
     * Google Play Services is missing, disabled, or too old to support
     * the Credential Manager API (requires Play Services 23.x+).
     */
    data class PlayServicesError(val message: String) : AuthResult()
}
