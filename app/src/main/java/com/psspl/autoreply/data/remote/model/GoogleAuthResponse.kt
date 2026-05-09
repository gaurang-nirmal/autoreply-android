package com.psspl.autoreply.data.remote.model

import com.google.gson.annotations.SerializedName
import com.psspl.autoreply.data.auth.model.AuthUser

/**
 * Response body from POST /auth/google.
 *
 * Matches the NestJS backend's LoginResponseDto:
 * ```json
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "user": {
 *     "id": "uuid-...",
 *     "name": "Jane Doe",
 *     "email": "jane@example.com",
 *     "profileImage": "https://...",
 *     "role": "user",
 *     "isActive": true
 *   }
 * }
 * ```
 */
data class GoogleAuthResponse(
    /** Short-lived app JWT for authenticated API calls (15 min TTL). */
    @SerializedName("accessToken")
    val accessToken: String,

    /** Long-lived refresh token for obtaining new access tokens (7 day TTL). */
    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("user")
    val user: RemoteUser,
)

data class RemoteUser(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?,

    /** Profile picture URL returned by the backend as "profileImage". */
    @SerializedName("profileImage")
    val profileImage: String?,
) {
    /** Maps the remote user to the app's domain model. */
    fun toAuthUser() = AuthUser(
        uid         = id,
        displayName = name,
        email       = email,
        photoUrl    = profileImage,
    )
}
