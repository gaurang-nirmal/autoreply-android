package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.DirectMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: DirectMessageEntity)

    @Query("SELECT * FROM direct_messages ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DirectMessageEntity>>

    @Query("DELETE FROM direct_messages")
    suspend fun deleteAll()
}
