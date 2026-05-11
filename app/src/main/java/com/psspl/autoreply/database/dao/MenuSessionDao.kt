package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.MenuSessionEntity

@Dao
interface MenuSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: MenuSessionEntity)

    @Query("SELECT * FROM menu_sessions WHERE contact_key = :contactKey")
    suspend fun getSession(contactKey: String): MenuSessionEntity?

    @Query("DELETE FROM menu_sessions WHERE contact_key = :contactKey")
    suspend fun deleteSession(contactKey: String)

    /** Prune sessions older than [cutoff] ms — called periodically from the engine. */
    @Query("DELETE FROM menu_sessions WHERE updated_at < :cutoff")
    suspend fun deleteExpiredSessions(cutoff: Long)
}
