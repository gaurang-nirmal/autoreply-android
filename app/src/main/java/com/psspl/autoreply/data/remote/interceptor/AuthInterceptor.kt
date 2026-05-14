package com.psspl.autoreply.data.remote.interceptor

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.psspl.autoreply.data.local.SessionManager
import com.psspl.autoreply.utils.AppConstants
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that:
 * 1. Attaches the stored Bearer token to every outgoing request.
 * 2. On a 401 response, silently refreshes the access token via
 *    POST /auth/refresh and retries the original request once.
 * 3. If refresh fails (invalid/expired refresh token), clears auth
 *    data — [SessionManager.currentUser] emits null and the app
 *    navigates to the sign-in screen automatically.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {

    // Prevents concurrent refresh calls from multiple threads
    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionManager.getJwtToken() }

        val response = chain.proceed(chain.request().withBearer(token))

        if (response.code != 401) return response

        // ── 401 received — try a silent token refresh ─────────────────────────
        response.close()

        val newAccessToken = synchronized(refreshLock) {
            // Re-read: another thread may have already refreshed while we waited
            val currentToken = runBlocking { sessionManager.getJwtToken() }
            if (currentToken != token && currentToken != null) {
                // Already refreshed by a concurrent request — reuse it
                currentToken
            } else {
                runBlocking { tryRefresh() }
            }
        } ?: return chain.proceed(chain.request()) // refresh failed → propagate 401

        return chain.proceed(chain.request().withBearer(newAccessToken))
    }

    /**
     * Calls POST /auth/refresh using a plain OkHttpClient (no interceptors)
     * to avoid an infinite retry loop.
     *
     * @return the new access token on success, or null on any failure.
     */
    private suspend fun tryRefresh(): String? {
        val refreshToken = sessionManager.getRefreshToken() ?: return null

        return try {
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${AppConstants.BASE_URL}auth/refresh")
                .post(body)
                .build()

            // Use a separate bare client — NOT the app's main client — to skip this interceptor
            val response = OkHttpClient().newCall(request).execute()

            if (response.isSuccessful) {
                val json = response.body?.string() ?: return null
                val tokens = Gson().fromJson(json, RefreshTokenResponse::class.java)
                sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                tokens.accessToken
            } else {
                // 401/403 from refresh endpoint = refresh token expired → sign out
                if (response.code == 401 || response.code == 403) {
                    sessionManager.clearAuthData()
                }
                null
            }
        } catch (e: Exception) {
            // Network error — don't sign the user out, just let the 401 propagate
            null
        }
    }

    private fun Request.withBearer(token: String?): Request =
        if (token != null) newBuilder().header("Authorization", "Bearer $token").build()
        else this
}

private data class RefreshTokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)
