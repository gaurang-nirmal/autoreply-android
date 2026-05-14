package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.SupportedAppDao
import com.psspl.autoreply.database.entity.SupportedAppEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportedAppsRepository @Inject constructor(
    private val dao: SupportedAppDao
) {
    val allApps: Flow<List<SupportedAppEntity>> = dao.getAll()

    val enabledApps: Flow<List<SupportedAppEntity>> = dao.getEnabled()

    suspend fun update(app: SupportedAppEntity) = dao.update(app)

    suspend fun isAppEnabled(appPackage: String): Boolean =
        dao.getByPackage(appPackage)?.isEnabled == true

    /** Returns the local app ID for a given package name, or null if not found. */
    suspend fun getAppIdByPackage(appPackage: String): Int? =
        dao.getByPackage(appPackage)?.id

    suspend fun seedDefaultApps(defaults: List<SupportedAppEntity>) {
        dao.insertAll(defaults)
    }
}
