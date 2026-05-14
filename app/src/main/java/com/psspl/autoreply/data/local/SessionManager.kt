package com.psspl.autoreply.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.psspl.autoreply.data.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the app-issued JWT and cached user profile in [DataStore].
 *
 * ## Security contract
 * - Only the **app JWT** (returned by the backend) is stored here.
 * - The Google ID token is intentionally never persisted.
 * - [DataStore] is the persistence layer; no in-memory copy of the JWT is kept.
 *
 * ## Auth state
 * [currentUser] emits a non-null [AuthUser] exactly when a JWT is present,
 * making it the single source of truth for the authenticated state that
 * [com.psspl.autoreply.ui.auth.AuthViewModel] observes.
 */
@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHOTO_URL = stringPreferencesKey("photo_url")

        // Google OAuth2 token for Sheets / Drive API access
        private val KEY_GOOGLE_ACCESS_TOKEN = stringPreferencesKey("google_access_token")
        private val KEY_GOOGLE_TOKEN_EXPIRY = stringPreferencesKey("google_token_expiry")
    }

    /**
     * Emits the authenticated [AuthUser] while a JWT is stored.
     * Emits null when signed out or on first launch.
     *
     * The first emission is asynchronous (DataStore reads from disk).
     * The UI shows a loading state while it waits for this first value.
     */
    val currentUser: Flow<AuthUser?> = dataStore.data.map { prefs ->
        // A JWT must be present to consider the user authenticated.
        val jwt = prefs[KEY_JWT_TOKEN] ?: return@map null
        val uid = prefs[KEY_USER_ID] ?: return@map null

        @Suppress("UNUSED_VARIABLE")  // jwt validated above; not exposed in domain model
        AuthUser(
            uid = uid,
            displayName = prefs[KEY_DISPLAY_NAME],
            email = prefs[KEY_EMAIL],
            photoUrl = prefs[KEY_PHOTO_URL],
        )
    }

    /** Raw JWT flow — for use in network interceptors. */
    val jwtToken: Flow<String?> = dataStore.data.map { it[KEY_JWT_TOKEN] }

    /**
     * Persists the JWT and user profile after a successful backend sign-in.
     * Triggers a [currentUser] emission which drives the UI to the
     * authenticated state.
     */
    suspend fun saveAuthData(jwt: String, refreshToken: String, user: AuthUser) {
        dataStore.edit { prefs ->
            prefs[KEY_JWT_TOKEN] = jwt
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = user.uid
            if (user.displayName != null) prefs[KEY_DISPLAY_NAME] = user.displayName
            if (user.email != null) prefs[KEY_EMAIL] = user.email
            if (user.photoUrl != null) prefs[KEY_PHOTO_URL] = user.photoUrl
        }
    }

    /** Overwrites both tokens after a silent refresh — does not touch user profile. */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_JWT_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    /** One-shot read of the stored refresh token. */
    suspend fun getRefreshToken(): String? =
        dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()

    /**
     * Wipes all auth data.
     * Triggers a [currentUser] emission of null — driving the UI to the
     * unauthenticated state without any additional navigation code.
     */
    suspend fun clearAuthData() {
        dataStore.edit { it.clear() }
    }

    /**
     * One-shot suspend read of the stored JWT.
     * Use this to attach the token to authenticated API requests.
     */
    suspend fun getJwtToken(): String? =
        dataStore.data.map { it[KEY_JWT_TOKEN] }.first()

    // ── Google OAuth2 (Sheets / Drive) ────────────────────────────────────────

    /**
     * Persists the Google OAuth2 access token and its expiry epoch-ms.
     * Called after a successful [Identity.getAuthorizationClient] authorization.
     */
    suspend fun saveGoogleAccessToken(token: String, expiryMs: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_GOOGLE_ACCESS_TOKEN] = token
            prefs[KEY_GOOGLE_TOKEN_EXPIRY] = expiryMs.toString()
        }
    }

    /**
     * Returns the stored Google OAuth2 access token, or null if none saved.
     * Check [isGoogleTokenValid] before using — expired tokens will be rejected by the API.
     */
    suspend fun getGoogleAccessToken(): String? =
        dataStore.data.map { it[KEY_GOOGLE_ACCESS_TOKEN] }.first()

    /**
     * Returns true when the stored access token exists and has not yet expired.
     * A 60-second buffer avoids using a token that expires during the API call.
     */
    suspend fun isGoogleTokenValid(): Boolean {
        val expiry =
            dataStore.data.map { it[KEY_GOOGLE_TOKEN_EXPIRY]?.toLongOrNull() ?: 0L }.first()
        return expiry > System.currentTimeMillis() + 60_000L
    }

    /** Clears the stored Google OAuth2 token (e.g. on sign-out). */
    suspend fun clearGoogleAccessToken() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_GOOGLE_ACCESS_TOKEN)
            prefs.remove(KEY_GOOGLE_TOKEN_EXPIRY)
        }
    }
}
