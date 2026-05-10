package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.ReplyLimitTrackingEntity
import com.psspl.autoreply.database.entity.ReplyTimingConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyTimingDao {

    // ─── Config ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM reply_timing_config WHERE reply_type = :replyType")
    fun getConfig(replyType: String): Flow<ReplyTimingConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: ReplyTimingConfigEntity)

    // ─── Limit tracking ───────────────────────────────────────────────────────

    @Query("SELECT * FROM reply_limit_tracking WHERE reply_type = :replyType ORDER BY last_reply_at DESC")
    fun getTrackingForType(replyType: String): Flow<List<ReplyLimitTrackingEntity>>

    @Query("SELECT * FROM reply_limit_tracking WHERE reply_type = :replyType AND contact_key = :contactKey")
    suspend fun getTracking(replyType: String, contactKey: String): ReplyLimitTrackingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracking(tracking: ReplyLimitTrackingEntity)

    @Query("DELETE FROM reply_limit_tracking WHERE reply_type = :replyType")
    suspend fun clearTrackingForType(replyType: String)

    @Query("DELETE FROM reply_limit_tracking WHERE reply_type = :replyType AND contact_key = :contactKey")
    suspend fun clearTrackingForContact(replyType: String, contactKey: String)
}
