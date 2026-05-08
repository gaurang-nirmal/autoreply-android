package com.psspl.autoreply.data.auth

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.psspl.autoreply.data.auth.model.AuthResult
import com.psspl.autoreply.data.auth.model.AuthUser
import com.psspl.autoreply.data.local.SessionManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleCredentialProvider: GoogleCredentialProvider,
    private val sessionManager: SessionManager,
) : AuthRepository {

    /**
     * Emits the current [AuthUser] whenever Firebase auth state changes.
     * Emits null when the user is signed out. The first emission happens
     * immediately on collection (Firebase calls the listener synchronously
     * upon registration if a current user already exists).
     */
    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(activityContext: Context): AuthResult {
        return try {
            val tokenResult = googleCredentialProvider.getGoogleIdToken(activityContext)

            if (tokenResult.isFailure) {
                val exception = tokenResult.exceptionOrNull()
                return when {
                    exception is GetCredentialCancellationException ->
                        AuthResult.Cancelled

                    exception is PlayServicesUnavailableException ->
                        AuthResult.PlayServicesError(
                            message = exception.message
                                ?: "Google Play Services is unavailable.",
                        )

                    else ->
                        AuthResult.Error(
                            message = exception?.message
                                ?: "Failed to retrieve Google credential",
                            cause = exception,
                        )
                }
            }

            val idToken = tokenResult.getOrThrow()
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

            val firebaseUser = authResult.user
                ?: return AuthResult.Error("Firebase returned no user after sign-in")

            sessionManager.saveSession(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName,
                email = firebaseUser.email,
                photoUrl = firebaseUser.photoUrl?.toString(),
            )

            AuthResult.Success(firebaseUser.toAuthUser(idToken = idToken))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-in failed", cause = e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        sessionManager.clearSession()
    }

    override fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    // ── Extension ──────────────────────────────────────────────────────────

    private fun FirebaseUser.toAuthUser(idToken: String? = null) = AuthUser(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString(),
        idToken = idToken,
    )
}
