package com.psspl.autoreply.data.repository

import com.psspl.autoreply.data.network.ApiService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves an Android package name to the backend's numeric app ID.
 *
 * Fetches GET /apps/active once and caches the result for the lifetime of the
 * process. The backend endpoint is public (no auth required), so this works
 * even before the user signs in.
 */
@Singleton
class AppIdResolver @Inject constructor(
    private val apiService: ApiService,
) {
    private val cache = mutableMapOf<String, Int>()
    private var loaded = false
    private val lock = Mutex()

    /**
     * Returns the backend app ID for the given package name, or null if the
     * app is not found or the fetch fails.
     */
    suspend fun resolve(packageName: String): Int? {
        if (!loaded) {
            lock.withLock {
                if (!loaded) {
                    try {
                        val response = apiService.getActiveApps()
                        if (response.isSuccessful) {
                            response.body()?.forEach { app ->
                                cache[app.packageName] = app.id
                            }
                            loaded = true
                        }
                    } catch (_: Exception) {
                        // Will retry on next call
                    }
                }
            }
        }
        return cache[packageName]
    }
}
