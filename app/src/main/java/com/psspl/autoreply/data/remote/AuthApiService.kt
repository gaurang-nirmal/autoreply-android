package com.psspl.autoreply.data.remote

import com.psspl.autoreply.data.remote.model.GoogleAuthRequest
import com.psspl.autoreply.data.remote.model.GoogleAuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service for authentication endpoints.
 *
 * Base URL is configured in [com.psspl.autoreply.di.NetworkModule].
 */
interface AuthApiService {

    /**
     * Exchange a Google ID token for an app-issued JWT.
     *
     * POST /auth/google
     *
     * Request:  { "idToken": "<google_id_token>" }
     * Response: { "token": "<jwt>", "user": { ... } }
     *
     * The backend must validate the ID token using Google's public keys before
     * issuing the JWT.
     *
     * @throws retrofit2.HttpException on non-2xx responses
     * @throws java.io.IOException on network failures
     */
    @POST("auth/google")
    suspend fun authenticateWithGoogle(
        @Body request: GoogleAuthRequest,
    ): GoogleAuthResponse
}
