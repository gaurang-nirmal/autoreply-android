package com.psspl.autoreply.di

import android.content.Context
import androidx.room.Room
import com.psspl.autoreply.database.AppDatabase
import com.psspl.autoreply.database.dao.AppSettingsDao
import com.psspl.autoreply.database.dao.ContactsDao
import com.psspl.autoreply.database.dao.DefaultMessageDao
import com.psspl.autoreply.database.dao.DirectMessageDao
import com.psspl.autoreply.database.dao.FollowUpDao
import com.psspl.autoreply.database.dao.KeywordRuleDao
import com.psspl.autoreply.database.dao.MenuReplyDao
import com.psspl.autoreply.database.dao.MenuSessionDao
import com.psspl.autoreply.database.dao.NoteDao
import com.psspl.autoreply.database.dao.ReplyNotificationDao
import com.psspl.autoreply.database.dao.ReplyTimingDao
import com.psspl.autoreply.database.dao.SpreadsheetDao
import com.psspl.autoreply.database.dao.SupportedAppDao
import com.psspl.autoreply.database.dao.WelcomeMessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "autoreply.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideKeywordRuleDao(db: AppDatabase): KeywordRuleDao = db.keywordRuleDao()

    @Provides
    fun provideAppSettingsDao(db: AppDatabase): AppSettingsDao = db.appSettingsDao()

    @Provides
    fun provideSupportedAppDao(db: AppDatabase): SupportedAppDao = db.supportedAppDao()

    @Provides
    fun provideReplyNotificationDao(db: AppDatabase): ReplyNotificationDao =
        db.replyNotificationDao()

    @Provides
    fun provideDirectMessageDao(db: AppDatabase): DirectMessageDao = db.directMessageDao()

    @Provides
    fun provideMenuReplyDao(db: AppDatabase): MenuReplyDao = db.menuReplyDao()

    @Provides
    fun provideMenuSessionDao(db: AppDatabase): MenuSessionDao = db.menuSessionDao()

    @Provides
    fun provideWelcomeMessageDao(db: AppDatabase): WelcomeMessageDao = db.welcomeMessageDao()

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideReplyTimingDao(db: AppDatabase): ReplyTimingDao = db.replyTimingDao()

    @Provides
    fun provideFollowUpDao(db: AppDatabase): FollowUpDao = db.followUpDao()

    @Provides
    fun provideDefaultMessageDao(db: AppDatabase): DefaultMessageDao = db.defaultMessageDao()

    @Provides
    fun provideSpreadsheetDao(db: AppDatabase): SpreadsheetDao = db.spreadsheetDao()

    @Provides
    fun provideContactsDao(db: AppDatabase): ContactsDao = db.contactsDao()
}
