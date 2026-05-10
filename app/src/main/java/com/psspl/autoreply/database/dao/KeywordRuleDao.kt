package com.psspl.autoreply.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordRuleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(rule: KeywordRuleEntity): Long

    @Update
    suspend fun update(rule: KeywordRuleEntity)

    @Delete
    suspend fun delete(rule: KeywordRuleEntity)

    @Query("SELECT * FROM keyword_rules ORDER BY created_at DESC")
    fun getAll(): Flow<List<KeywordRuleEntity>>

    @Query("SELECT * FROM keyword_rules WHERE is_active = 1 ORDER BY created_at DESC")
    fun getActive(): Flow<List<KeywordRuleEntity>>

    @Query("SELECT * FROM keyword_rules WHERE id = :id")
    fun getById(id: Int): Flow<KeywordRuleEntity?>

    // Returns rules for a specific app plus any global rules (app_package = '')
    @Query("SELECT * FROM keyword_rules WHERE (app_package = :appPackage OR app_package = '') AND is_active = 1")
    fun getActiveForApp(appPackage: String): Flow<List<KeywordRuleEntity>>
}
