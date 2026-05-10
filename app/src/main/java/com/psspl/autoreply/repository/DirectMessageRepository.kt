package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.DirectMessageDao
import com.psspl.autoreply.database.entity.DirectMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectMessageRepository @Inject constructor(
    private val dao: DirectMessageDao
) {
    val allHistory: Flow<List<DirectMessageEntity>> = dao.getAll()

    suspend fun insert(message: DirectMessageEntity) = dao.insert(message)

    suspend fun deleteAll() = dao.deleteAll()
}
