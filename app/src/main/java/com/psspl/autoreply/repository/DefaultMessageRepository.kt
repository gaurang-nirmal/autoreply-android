package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.DefaultMessageDao
import com.psspl.autoreply.database.entity.DefaultMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMessageRepository @Inject constructor(
    private val dao: DefaultMessageDao,
) {
    companion object {
        private val SEED_MESSAGES = listOf(
            "I am busy, text you later.",
            "I am driving, text you later.",
            "I am sleeping, text you later.",
            "Can't talk now.",
            "At the movie, text you later.",
            "At work, text you later.",
            "In a meeting, text you later.",
        )
    }

    fun getAllMessages(): Flow<List<DefaultMessageEntity>> = dao.getAllMessages()

    /** Seeds default messages on first launch (no-op if table is already populated). */
    suspend fun seedIfEmpty() {
        if (dao.getCount() == 0) {
            dao.insertAll(SEED_MESSAGES.map { text ->
                DefaultMessageEntity(message = text, isDefault = true)
            })
        }
    }

    suspend fun delete(message: DefaultMessageEntity) = dao.delete(message)

    /** Deletes all non-seeded messages; preserves isDefault=true rows. */
    suspend fun clearCustomMessages() = dao.clearCustomMessages()
}
