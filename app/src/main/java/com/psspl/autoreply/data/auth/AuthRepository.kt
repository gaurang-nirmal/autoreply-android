package com.psspl.autoreply.data.auth

import android.content.Context
import com.psspl.autoreply.data.auth.model.AuthResult
import com.psspl.autoreply.data.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Hot flow driven by [SessionManager].
     * Emits a non-null [AuthUser] while a valid JWT is stored locally.
     * Emits null after sign-out or when no session exists.
     */
    val currentUser: Flow<AuthUser?>

    /**
     * Launches the Google account-picker via Credential Manager, sends the
     * resulting ID token to the backend, and persists the returned JWT.
     *
     * @param activityContext Must be an Activity context so Credential Manager
     *   can display the account-picker bottom-sheet.
     */
    suspend fun signInWithGoogle(activityContext: Context): AuthResult

    /** Clears the locally stored JWT and user data. */
    suspend fun signOut()

    /**
     * Returns the stored app JWT for use in authenticated API requests.
     * Returns null if the user is not signed in.
     */
    suspend fun getJwtToken(): String?

    /**
     * Permanently deletes the user's account on the backend and clears local session.
     * Returns true on success, false on failure.
     */
    suspend fun deleteAccount(): Result<Unit>
}
