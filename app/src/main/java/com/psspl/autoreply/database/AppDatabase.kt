package com.psspl.autoreply.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.psspl.autoreply.database.dao.AppSettingsDao
import com.psspl.autoreply.database.dao.DirectMessageDao
import com.psspl.autoreply.database.dao.KeywordRuleDao
import com.psspl.autoreply.database.dao.ReplyNotificationDao
import com.psspl.autoreply.database.dao.SupportedAppDao
import com.psspl.autoreply.database.entity.AppSettingsEntity
import com.psspl.autoreply.database.entity.DirectMessageEntity
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.database.entity.SupportedAppEntity

@Database(
    entities = [
        KeywordRuleEntity::class,
        AppSettingsEntity::class,
        SupportedAppEntity::class,
        ReplyNotificationEntity::class,
        DirectMessageEntity::class,
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keywordRuleDao(): KeywordRuleDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun supportedAppDao(): SupportedAppDao
    abstract fun replyNotificationDao(): ReplyNotificationDao
    abstract fun directMessageDao(): DirectMessageDao
}
