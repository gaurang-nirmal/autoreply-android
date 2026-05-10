package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.FollowUpDao
import com.psspl.autoreply.database.entity.FollowUpConfigEntity
import com.psspl.autoreply.database.entity.FollowUpContactEntity
import com.psspl.autoreply.database.entity.FollowUpHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUpRepository @Inject constructor(
    private val dao: FollowUpDao,
) {
    val config: Flow<FollowUpConfigEntity?> = dao.observeConfig()
    val history: Flow<List<FollowUpHistoryEntity>> = dao.observeHistory()

    fun contacts(listType: String): Flow<List<FollowUpContactEntity>> =
        dao.observeContacts(listType)

    private suspend fun getOrDefault(): FollowUpConfigEntity =
        dao.getConfig() ?: FollowUpConfigEntity()

    suspend fun toggleEnabled() {
        val c = getOrDefault()
        dao.upsertConfig(c.copy(isEnabled = !c.isEnabled))
    }

    suspend fun setMessage(message: String) {
        val c = getOrDefault()
        dao.upsertConfig(c.copy(message = message))
    }

    suspend fun setScope(scope: String) {
        val c = getOrDefault()
        dao.upsertConfig(c.copy(scope = scope))
    }

    suspend fun insertHistory(history: FollowUpHistoryEntity): Long =
        dao.insertHistory(history)

    suspend fun deleteHistory(history: FollowUpHistoryEntity) =
        dao.deleteHistory(history)

    suspend fun clearHistory() = dao.clearHistory()

    suspend fun insertContact(contact: FollowUpContactEntity) =
        dao.insertContact(contact)

    suspend fun deleteContact(contact: FollowUpContactEntity) =
        dao.deleteContact(contact)

    suspend fun clearContacts(listType: String) = dao.clearContacts(listType)
}
