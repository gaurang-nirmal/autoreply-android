package com.psspl.autoreply.ui.auth

import com.psspl.autoreply.data.auth.model.AuthUser

/**
 * Represents the root-level authentication state consumed by [AuthViewModel]
 * and observed in [MainActivity] to decide which UI branch to display.
 */
sealed class AuthState {
    /** Firebase session check is in progress — show a loading indicator. */
    data object Loading : AuthState()

    /** A valid session exists. [user] contains the cached profile. */
    data class Authenticated(val user: AuthUser) : AuthState()

    /** No valid session — navigate the user to the login screen. */
    data object Unauthenticated : AuthState()
}
