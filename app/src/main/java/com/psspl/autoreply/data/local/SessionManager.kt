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
        private val KEY_JWT_TOKEN    = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID      = stringPreferencesKey("user_id")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL        = stringPreferencesKey("email")
        private val KEY_PHOTO_URL    = stringPreferencesKey("photo_url")
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
        val uid = prefs[KEY_USER_ID]   ?: return@map null

        @Suppress("UNUSED_VARIABLE")  // jwt validated above; not exposed in domain model
        AuthUser(
            uid         = uid,
            displayName = prefs[KEY_DISPLAY_NAME],
            email       = prefs[KEY_EMAIL],
            photoUrl    = prefs[KEY_PHOTO_URL],
        )
    }

    /** Raw JWT flow — for use in network interceptors. */
    val jwtToken: Flow<String?> = dataStore.data.map { it[KEY_JWT_TOKEN] }

    /**
     * Persists the JWT and user profile after a successful backend sign-in.
     * Triggers a [currentUser] emission which drives the UI to the
     * authenticated state.
     */
    suspend fun saveAuthData(jwt: String, user: AuthUser) {
        dataStore.edit { prefs ->
            prefs[KEY_JWT_TOKEN] = jwt
            prefs[KEY_USER_ID]   = user.uid
            if (user.displayName != null) prefs[KEY_DISPLAY_NAME] = user.displayName
            if (user.email       != null) prefs[KEY_EMAIL]        = user.email
            if (user.photoUrl    != null) prefs[KEY_PHOTO_URL]    = user.photoUrl
        }
    }

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
}
