package com.psspl.autoreply.data.auth

import android.content.Context
import com.psspl.autoreply.data.auth.model.AuthResult
import com.psspl.autoreply.data.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Hot flow that emits the current authenticated user, or null when signed out.
     * Backed by [com.google.firebase.auth.FirebaseAuth.AuthStateListener].
     */
    val currentUser: Flow<AuthUser?>

    /**
     * Launches the Google account-picker via Credential Manager and signs the
     * selected account into Firebase.
     *
     * @param activityContext Must be an Activity context so Credential Manager
     *   can display the account-picker bottom-sheet.
     */
    suspend fun signInWithGoogle(activityContext: Context): AuthResult

    /** Signs out from Firebase and clears the local session. */
    suspend fun signOut()

    /** Synchronous check — true if Firebase reports a current user. */
    fun isLoggedIn(): Boolean
}
