package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: ReplyNotificationEntity)

    @Query("SELECT * FROM reply_notifications ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ReplyNotificationEntity>>

    @Query("SELECT COUNT(*) FROM reply_notifications WHERE timestamp > :since")
    fun countUnread(since: Long): Flow<Int>

    @Query("DELETE FROM reply_notifications")
    suspend fun deleteAll()
}
