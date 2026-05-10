package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.psspl.autoreply.database.entity.SupportedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: SupportedAppEntity): Long

    // Used for seeding — skips rows that already exist so user toggles are preserved
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<SupportedAppEntity>)

    @Update
    suspend fun update(app: SupportedAppEntity)

    @Delete
    suspend fun delete(app: SupportedAppEntity)

    @Query("SELECT * FROM supported_apps ORDER BY display_name ASC")
    fun getAll(): Flow<List<SupportedAppEntity>>

    @Query("SELECT * FROM supported_apps WHERE is_enabled = 1 ORDER BY display_name ASC")
    fun getEnabled(): Flow<List<SupportedAppEntity>>

    @Query("SELECT * FROM supported_apps WHERE app_package = :appPackage")
    suspend fun getByPackage(appPackage: String): SupportedAppEntity?
}
