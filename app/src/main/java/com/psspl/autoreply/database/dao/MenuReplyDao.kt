package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.psspl.autoreply.database.entity.MenuReplyEntity
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuReplyDao {

    // ─── Menu Replies (triggers) ──────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: MenuReplyEntity): Long

    @Update
    suspend fun updateReply(reply: MenuReplyEntity)

    @Delete
    suspend fun deleteReply(reply: MenuReplyEntity)

    @Query("SELECT * FROM menu_replies ORDER BY created_at DESC")
    fun getAllReplies(): Flow<List<MenuReplyEntity>>

    @Query("SELECT * FROM menu_replies WHERE id = :id")
    fun getReplyById(id: Int): Flow<MenuReplyEntity?>

    // ─── Menu Items ───────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MenuReplyItemEntity): Long

    @Update
    suspend fun updateItem(item: MenuReplyItemEntity)

    @Delete
    suspend fun deleteItem(item: MenuReplyItemEntity)

    /** All top-level items (parent_item_id IS NULL) across all replies — used for combined state */
    @Query("SELECT * FROM menu_reply_items WHERE parent_item_id IS NULL ORDER BY menu_reply_id, position ASC")
    fun getAllTopLevelItems(): Flow<List<MenuReplyItemEntity>>

    /** Every item row — used to check hasChildren */
    @Query("SELECT * FROM menu_reply_items ORDER BY menu_reply_id, position ASC")
    fun getAllItems(): Flow<List<MenuReplyItemEntity>>

    @Query("SELECT * FROM menu_reply_items WHERE id = :id")
    fun getItemById(id: Int): Flow<MenuReplyItemEntity?>

    /** Count top-level items for a given reply (to assign next position) */
    @Query("SELECT COUNT(*) FROM menu_reply_items WHERE menu_reply_id = :menuReplyId AND parent_item_id IS NULL")
    suspend fun getTopLevelItemCount(menuReplyId: Int): Int

    /** Count child items of a parent item (to assign next position) */
    @Query("SELECT COUNT(*) FROM menu_reply_items WHERE parent_item_id = :parentItemId")
    suspend fun getChildItemCount(parentItemId: Int): Int

    /** Cascade-delete all items belonging to a reply (used before deleting the reply itself) */
    @Query("DELETE FROM menu_reply_items WHERE menu_reply_id = :menuReplyId")
    suspend fun deleteAllItemsForReply(menuReplyId: Int)

    /** Delete direct children of a menu item (used before deleting the item) */
    @Query("DELETE FROM menu_reply_items WHERE parent_item_id = :parentItemId")
    suspend fun deleteChildItems(parentItemId: Int)

    /** Delete every reply row — items cascade-delete via FK */
    @Query("DELETE FROM menu_replies")
    suspend fun deleteAllReplies()

    // ─── Engine queries (suspend, non-Flow) ───────────────────────────────────

    /** All active trigger rows — used by MenuReplyEngine for incoming message matching. */
    @Query("SELECT * FROM menu_replies WHERE is_active = 1 ORDER BY created_at DESC")
    suspend fun getActiveRepliesOnce(): List<MenuReplyEntity>

    /** Top-level items (no parent) for a trigger — sent as the initial menu. */
    @Query("SELECT * FROM menu_reply_items WHERE menu_reply_id = :menuReplyId AND parent_item_id IS NULL ORDER BY position ASC")
    suspend fun getTopLevelItemsOnce(menuReplyId: Int): List<MenuReplyItemEntity>

    /** Direct children of a menu item — sent when a user navigates into a sub-menu. */
    @Query("SELECT * FROM menu_reply_items WHERE parent_item_id = :parentItemId ORDER BY position ASC")
    suspend fun getItemsByParentOnce(parentItemId: Int): List<MenuReplyItemEntity>

    /** Single item lookup by id — used to resolve a selected item's metadata. */
    @Query("SELECT * FROM menu_reply_items WHERE id = :id")
    suspend fun getItemByIdOnce(id: Int): MenuReplyItemEntity?
}
