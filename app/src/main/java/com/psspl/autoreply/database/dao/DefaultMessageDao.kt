package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.DefaultMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultMessageDao {

    @Query("SELECT * FROM default_messages ORDER BY is_default DESC, created_at ASC")
    fun getAllMessages(): Flow<List<DefaultMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<DefaultMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: DefaultMessageEntity): Long

    @Delete
    suspend fun delete(message: DefaultMessageEntity)

    /** Deletes only user-added messages; seeded defaults (isDefault=true) are preserved. */
    @Query("DELETE FROM default_messages WHERE is_default = 0")
    suspend fun clearCustomMessages()

    @Query("SELECT COUNT(*) FROM default_messages")
    suspend fun getCount(): Int
}
