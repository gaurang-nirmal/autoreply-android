package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keyword_rules")
data class KeywordRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val keyword: String,

    @ColumnInfo(name = "reply_text")
    val replyText: String,

    // empty string means rule applies to all apps
    @ColumnInfo(name = "app_package")
    val appPackage: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
