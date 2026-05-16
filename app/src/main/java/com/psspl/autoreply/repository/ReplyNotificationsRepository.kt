package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.ReplyNotificationDao
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplyNotificationsRepository @Inject constructor(
    private val dao: ReplyNotificationDao
) {
    val allNotifications: Flow<List<ReplyNotificationEntity>> = dao.getAll()

    fun countUnread(since: Long): Flow<Int> = dao.countUnread(since)

    fun getByReplyText(replyText: String): Flow<List<ReplyNotificationEntity>> =
        dao.getByReplyText(replyText)

    suspend fun insert(notification: ReplyNotificationEntity) = dao.insert(notification)

    suspend fun deleteAll() = dao.deleteAll()
}
