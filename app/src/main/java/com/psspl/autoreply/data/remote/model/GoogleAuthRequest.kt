package com.psspl.autoreply.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /auth/google.
 *
 * Only the raw Google ID token is sent to the backend.
 * The backend is responsible for validating it with Google's public keys.
 */
data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String,
)
