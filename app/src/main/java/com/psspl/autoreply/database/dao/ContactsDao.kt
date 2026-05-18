package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psspl.autoreply.database.entity.ContactConfigEntity
import com.psspl.autoreply.database.entity.ContactEntity
import com.psspl.autoreply.database.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    // ── Config (single-row upsert) ────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: ContactConfigEntity)

    @Query("SELECT * FROM contact_config WHERE id = 1")
    fun observeConfig(): Flow<ContactConfigEntity?>

    @Query("SELECT * FROM contact_config WHERE id = 1")
    suspend fun getConfig(): ContactConfigEntity?

    // ── Contacts ──────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Query("SELECT * FROM contact_filter ORDER BY added_at DESC")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contact_filter ORDER BY added_at DESC")
    suspend fun getAllContacts(): List<ContactEntity>

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contact_filter")
    suspend fun clearContacts()

    // ── Groups ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Query("SELECT * FROM contact_group ORDER BY added_at DESC")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM contact_group ORDER BY added_at DESC")
    suspend fun getAllGroups(): List<GroupEntity>

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM contact_group")
    suspend fun clearGroups()
}
