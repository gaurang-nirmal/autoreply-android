package com.psspl.autoreply.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.psspl.autoreplyclone.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thrown when Google Play Services is unavailable or too old to support the
 * Credential Manager provider (requires Play Services 23.x+).
 */
class PlayServicesUnavailableException(message: String) : Exception(message)

/**
 * Wraps the Credential Manager API to retrieve a Google ID token.
 *
 * Uses the modern GIS (Google Identity Services) flow — NOT the deprecated
 * [com.google.android.gms.auth.api.signin.GoogleSignIn] API.
 *
 * ## Pre-requisite
 * Credential Manager delegates its Google provider to Google Play Services.
 * The device must have **Play Services 23.x or later** (versionCode ≥ 230000000).
 * Older devices/emulators will receive [PlayServicesUnavailableException].
 */
@Singleton
class GoogleCredentialProvider @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    // CredentialManager can be created with Application context;
    // only getCredential() requires an Activity context for picker UI.
    private val credentialManager = CredentialManager.create(appContext)

    /**
     * Attempts to retrieve a Google ID token for the user.
     *
     * **Strategy**
     * 1. Guard: verify Google Play Services is up-to-date.
     * 2. Silent phase (`filterByAuthorizedAccounts = true`): returns a previously
     *    used account with zero UI if exactly one account is eligible.
     * 3. Picker phase (`filterByAuthorizedAccounts = false`): shows the full
     *    account-picker bottom-sheet when the silent phase finds no accounts.
     *
     * @param activityContext Activity context required for the picker bottom-sheet.
     * @return [Result.success] with the raw ID token string, or [Result.failure].
     */
    suspend fun getGoogleIdToken(activityContext: Context): Result<String> {
        // ── Phase 1: silent / zero-tap sign-in ───────────────────────────
        val silentResult = fetchToken(
            activityContext = activityContext,
            filterByAuthorizedAccounts = true,
        )
        if (silentResult.isSuccess) return silentResult

        val silentError = silentResult.exceptionOrNull()
        // Only fall through to the account-picker on NoCredentialException.
        // Every other failure (Unsupported, Cancelled, …) is propagated as-is.
        if (silentError !is NoCredentialException) return silentResult

        // ── Phase 2: full account picker ─────────────────────────────────
        return fetchToken(
            activityContext = activityContext,
            filterByAuthorizedAccounts = false,
        )
    }

    // ── Private ───────────────────────────────────────────────────────────

    private suspend fun fetchToken(
        activityContext: Context,
        filterByAuthorizedAccounts: Boolean,
    ): Result<String> {
        val webClientId = appContext.getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(filterByAuthorizedAccounts)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = credentialManager.getCredential(
                context = activityContext,
                request = request,
            )
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(
                    IllegalStateException("Unexpected credential type: ${credential.type}")
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: GetCredentialUnsupportedException) {
            // Credential Manager found no registered provider — typically means
            // the device's Play Services version is still too old even after our
            // guard check (race condition or custom ROM).
            Result.failure(
                PlayServicesUnavailableException(
                    "Google Sign-In provider is unavailable on this device. " +
                    "Please update Google Play Services from the Play Store.",
                )
            )
        } catch (e: GetCredentialException) {
            Result.failure(e)
        }
    }
}
