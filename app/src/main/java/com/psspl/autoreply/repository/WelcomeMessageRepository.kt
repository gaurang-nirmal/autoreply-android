package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.WelcomeMessageDao
import com.psspl.autoreply.database.entity.WelcomeMessageConfigEntity
import com.psspl.autoreply.database.entity.WelcomeMessageContactEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WelcomeMessageRepository @Inject constructor(
    private val dao: WelcomeMessageDao,
) {

    // ─── Config ───────────────────────────────────────────────────────────────

    fun getConfig(): Flow<WelcomeMessageConfigEntity?> = dao.getConfig()

    suspend fun upsertConfig(config: WelcomeMessageConfigEntity) = dao.upsertConfig(config)

    // ─── Contact tracking ─────────────────────────────────────────────────────

    suspend fun getContactRecord(
        appPackage: String,
        contactKey: String,
    ): WelcomeMessageContactEntity? = dao.getContactRecord(appPackage, contactKey)

    suspend fun upsertContactRecord(contact: WelcomeMessageContactEntity) =
        dao.upsertContactRecord(contact)

    suspend fun clearAllContacts() = dao.clearAllContacts()

    /**
     * Returns true when a welcome message should be sent to this contact.
     * True if the contact has never received a welcome message, or the cooldown has elapsed.
     */
    suspend fun shouldSendWelcome(
        appPackage: String,
        contactKey: String,
        cooldownDays: Int,
    ): Boolean {
        val record = dao.getContactRecord(appPackage, contactKey) ?: return true
        val cooldownMs = cooldownDays * 24L * 60 * 60 * 1000
        return System.currentTimeMillis() - record.lastSentAt >= cooldownMs
    }

    /** Records that a welcome message was just sent to this contact. */
    suspend fun recordWelcomeSent(appPackage: String, contactKey: String) {
        dao.upsertContactRecord(
            WelcomeMessageContactEntity(
                appPackage = appPackage,
                contactKey = contactKey,
                lastSentAt = System.currentTimeMillis(),
            )
        )
    }
}
