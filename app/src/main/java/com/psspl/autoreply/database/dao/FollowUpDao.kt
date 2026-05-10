package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.FollowUpConfigEntity
import com.psspl.autoreply.database.entity.FollowUpContactEntity
import com.psspl.autoreply.database.entity.FollowUpHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {

    // ── Config (single-row upsert) ────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: FollowUpConfigEntity)

    @Query("SELECT * FROM follow_up_config WHERE id = 1")
    fun observeConfig(): Flow<FollowUpConfigEntity?>

    @Query("SELECT * FROM follow_up_config WHERE id = 1")
    suspend fun getConfig(): FollowUpConfigEntity?

    // ── History ───────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: FollowUpHistoryEntity): Long

    @Query("SELECT * FROM follow_up_history ORDER BY scheduled_at DESC")
    fun observeHistory(): Flow<List<FollowUpHistoryEntity>>

    @Delete
    suspend fun deleteHistory(history: FollowUpHistoryEntity)

    @Query("DELETE FROM follow_up_history")
    suspend fun clearHistory()

    // ── Contact / filter lists ────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: FollowUpContactEntity)

    @Query("SELECT * FROM follow_up_contact WHERE list_type = :listType ORDER BY added_at DESC")
    fun observeContacts(listType: String): Flow<List<FollowUpContactEntity>>

    @Delete
    suspend fun deleteContact(contact: FollowUpContactEntity)

    @Query("DELETE FROM follow_up_contact WHERE list_type = :listType")
    suspend fun clearContacts(listType: String)
}
