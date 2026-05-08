package com.psspl.autoreply.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists lightweight session data in [DataStore] so the UI can show a
 * cached user profile even before the Firebase auth listener fires.
 *
 * Firebase Auth is still the authoritative source of truth for the session.
 * This store is used only for supplementary persistence.
 */
@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USER_UID = stringPreferencesKey("user_uid")
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_PHOTO_URL = stringPreferencesKey("photo_url")
    }

    /** Emits true while a session record exists in the local store. */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userUid: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_USER_UID] }

    val displayName: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_DISPLAY_NAME] }

    val email: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_EMAIL] }

    val photoUrl: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_PHOTO_URL] }

    /** Called after a successful sign-in. Caches the user profile locally. */
    suspend fun saveSession(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?,
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_UID] = uid
            if (displayName != null) prefs[KEY_DISPLAY_NAME] = displayName
            if (email != null) prefs[KEY_EMAIL] = email
            if (photoUrl != null) prefs[KEY_PHOTO_URL] = photoUrl
        }
    }

    /** Called on sign-out. Wipes all locally stored session data. */
    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
