package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.KeywordRuleDao
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordRuleRepository @Inject constructor(
    private val dao: KeywordRuleDao
) {
    val allRules: Flow<List<KeywordRuleEntity>> = dao.getAll()

    val activeRules: Flow<List<KeywordRuleEntity>> = dao.getActive()

    fun getById(id: Int): Flow<KeywordRuleEntity?> = dao.getById(id)

    fun getActiveForApp(appPackage: String): Flow<List<KeywordRuleEntity>> =
        dao.getActiveForApp(appPackage)

    suspend fun insert(rule: KeywordRuleEntity): Long = dao.insert(rule)

    suspend fun update(rule: KeywordRuleEntity) = dao.update(rule)

    suspend fun delete(rule: KeywordRuleEntity) = dao.delete(rule)

    suspend fun deleteAll() = dao.deleteAll()
}
