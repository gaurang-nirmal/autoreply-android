package com.psspl.autoreply.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.psspl.autoreply.database.dao.AppSettingsDao
import com.psspl.autoreply.database.dao.DirectMessageDao
import com.psspl.autoreply.database.dao.KeywordRuleDao
import com.psspl.autoreply.database.dao.MenuReplyDao
import com.psspl.autoreply.database.dao.NoteDao
import com.psspl.autoreply.database.dao.ReplyNotificationDao
import com.psspl.autoreply.database.dao.ReplyTimingDao
import com.psspl.autoreply.database.dao.SupportedAppDao
import com.psspl.autoreply.database.dao.WelcomeMessageDao
import com.psspl.autoreply.database.entity.AppSettingsEntity
import com.psspl.autoreply.database.entity.DirectMessageEntity
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.database.entity.MenuReplyEntity
import com.psspl.autoreply.database.entity.MenuReplyItemEntity
import com.psspl.autoreply.database.entity.NoteEntity
import com.psspl.autoreply.database.entity.ReplyLimitTrackingEntity
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.database.entity.ReplyTimingConfigEntity
import com.psspl.autoreply.database.entity.SupportedAppEntity
import com.psspl.autoreply.database.entity.WelcomeMessageConfigEntity
import com.psspl.autoreply.database.entity.WelcomeMessageContactEntity

@Database(
    entities = [
        KeywordRuleEntity::class,
        AppSettingsEntity::class,
        SupportedAppEntity::class,
        ReplyNotificationEntity::class,
        DirectMessageEntity::class,
        MenuReplyEntity::class,
        MenuReplyItemEntity::class,
        WelcomeMessageConfigEntity::class,
        WelcomeMessageContactEntity::class,
        NoteEntity::class,
        ReplyTimingConfigEntity::class,
        ReplyLimitTrackingEntity::class,
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keywordRuleDao(): KeywordRuleDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun supportedAppDao(): SupportedAppDao
    abstract fun replyNotificationDao(): ReplyNotificationDao
    abstract fun directMessageDao(): DirectMessageDao
    abstract fun menuReplyDao(): MenuReplyDao
    abstract fun welcomeMessageDao(): WelcomeMessageDao
    abstract fun noteDao(): NoteDao
    abstract fun replyTimingDao(): ReplyTimingDao
}
