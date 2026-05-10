package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.MenuReplyDao
import com.psspl.autoreply.database.entity.MenuReplyEntity
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuReplyRepository @Inject constructor(
    private val dao: MenuReplyDao,
) {
    // ─── Replies ──────────────────────────────────────────────────────────────

    fun getAllReplies(): Flow<List<MenuReplyEntity>> = dao.getAllReplies()

    fun getReplyById(id: Int): Flow<MenuReplyEntity?> = dao.getReplyById(id)

    suspend fun insertReply(reply: MenuReplyEntity): Long = dao.insertReply(reply)

    suspend fun updateReply(reply: MenuReplyEntity) = dao.updateReply(reply)

    suspend fun deleteReply(reply: MenuReplyEntity) = dao.deleteReply(reply)

    // ─── Items ────────────────────────────────────────────────────────────────

    fun getAllTopLevelItems(): Flow<List<MenuReplyItemEntity>> = dao.getAllTopLevelItems()

    fun getAllItems(): Flow<List<MenuReplyItemEntity>> = dao.getAllItems()

    fun getItemById(id: Int): Flow<MenuReplyItemEntity?> = dao.getItemById(id)

    suspend fun insertItem(item: MenuReplyItemEntity): Long = dao.insertItem(item)

    suspend fun updateItem(item: MenuReplyItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: MenuReplyItemEntity) = dao.deleteItem(item)

    suspend fun getTopLevelItemCount(menuReplyId: Int): Int = dao.getTopLevelItemCount(menuReplyId)

    suspend fun getChildItemCount(parentItemId: Int): Int = dao.getChildItemCount(parentItemId)

    suspend fun deleteAllItemsForReply(menuReplyId: Int) = dao.deleteAllItemsForReply(menuReplyId)

    suspend fun deleteChildItems(parentItemId: Int) = dao.deleteChildItems(parentItemId)

    suspend fun deleteAllReplies() = dao.deleteAllReplies()
}
