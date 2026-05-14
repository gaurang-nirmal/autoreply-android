package com.psspl.autoreply.data.auth

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.psspl.autoreply.data.auth.model.AuthResult
import com.psspl.autoreply.data.auth.model.AuthUser
import com.psspl.autoreply.data.local.SessionManager
import com.psspl.autoreply.data.remote.AuthApiService
import com.psspl.autoreply.data.remote.model.GoogleAuthRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [AuthRepository].
 *
 * ## Sign-in flow
 * 1. Credential Manager retrieves a Google ID token via GIS (no Firebase).
 * 2. ID token is sent to the backend POST /auth/google.
 * 3. Backend validates with Google's public keys and returns an app JWT.
 * 4. JWT + user profile are persisted in [SessionManager] (DataStore).
 * 5. [currentUser] flow emits — driving the UI to the authenticated state.
 *
 * ## Session restore
 * On cold start the [currentUser] flow reads from DataStore.  If a JWT
 * is present the user is considered authenticated without any network call.
 *
 * ## Security
 * - The Google ID token is used exactly once (to exchange for a JWT) and
 *   is never stored locally.
 * - Only the app-issued JWT is persisted.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val googleCredentialProvider: GoogleCredentialProvider,
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager,
) : AuthRepository {

    /** Backed by DataStore — emits the cached [AuthUser] while a JWT is stored. */
    override val currentUser: Flow<AuthUser?> = sessionManager.currentUser

    override suspend fun signInWithGoogle(activityContext: Context): AuthResult {
        return try {
            // ── Step 1: Google ID token via Credential Manager ────────────
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
                                ?: "Failed to retrieve Google credential.",
                            cause = exception,
                        )
                }
            }

            val idToken = tokenResult.getOrThrow()

            // ── Step 2: Exchange ID token for app JWT via backend ─────────
            val response = authApiService.authenticateWithGoogle(
                GoogleAuthRequest(idToken = idToken),
            )

            // ── Step 3: Persist JWT + user profile (NOT the Google token) ─
            val user = response.user.toAuthUser()
            sessionManager.saveAuthData(
                jwt = response.accessToken,
                refreshToken = response.refreshToken,
                user = user,
            )

            AuthResult.Success(user)

        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Authentication rejected by server. Please try again."
                403 -> "Access denied. Contact support if this persists."
                500 -> "Server error. Please try again later."
                else -> "Sign-in failed (HTTP ${e.code()}). Please try again."
            }
            AuthResult.Error(message = message, cause = e)
        } catch (e: IOException) {
            AuthResult.Error(
                message = "No internet connection. Please check your network and try again.",
                cause = e,
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unexpected error occurred.", cause = e)
        }
    }

    override suspend fun signOut() {
        sessionManager.clearAuthData()
    }

    override suspend fun getJwtToken(): String? = sessionManager.getJwtToken()
}
