package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.WelcomeMessageConfigEntity
import com.psspl.autoreply.database.entity.WelcomeMessageContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WelcomeMessageDao {

    // ─── Config (single row, id = 1) ─────────────────────────────────────────

    @Query("SELECT * FROM welcome_message_config WHERE id = 1")
    fun getConfig(): Flow<WelcomeMessageConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: WelcomeMessageConfigEntity)

    // ─── Contact tracking ─────────────────────────────────────────────────────

    @Query("SELECT * FROM welcome_message_contacts WHERE app_package = :appPackage AND contact_key = :contactKey")
    suspend fun getContactRecord(
        appPackage: String,
        contactKey: String
    ): WelcomeMessageContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContactRecord(contact: WelcomeMessageContactEntity)

    /** Wipe all contact timestamps — used by a "Reset tracking" action */
    @Query("DELETE FROM welcome_message_contacts")
    suspend fun clearAllContacts()
}
