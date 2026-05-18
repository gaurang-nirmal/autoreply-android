package com.psspl.autoreply.repository

import android.content.Context
import android.provider.ContactsContract
import com.psspl.autoreply.database.dao.ContactsDao
import com.psspl.autoreply.database.entity.ContactConfigEntity
import com.psspl.autoreply.database.entity.ContactEntity
import com.psspl.autoreply.database.entity.GroupEntity
import com.psspl.autoreply.utils.AppLogger
import com.psspl.autoreply.utils.ContactMode
import com.psspl.autoreply.utils.GroupMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    private val dao: ContactsDao,
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val TAG = "ContactsRepository"
    }

    // ── Config ────────────────────────────────────────────────────────────────

    fun observeConfig(): Flow<ContactConfigEntity?> = dao.observeConfig()

    suspend fun getConfig(): ContactConfigEntity = dao.getConfig() ?: ContactConfigEntity()

    suspend fun upsertConfig(config: ContactConfigEntity) = dao.upsertConfig(config)

    suspend fun setContactMode(mode: ContactMode) {
        val current = getConfig()
        dao.upsertConfig(current.copy(contactMode = mode.name))
    }

    suspend fun setGroupsEnabled(enabled: Boolean) {
        val current = getConfig()
        dao.upsertConfig(current.copy(groupsEnabled = enabled))
    }

    suspend fun setGroupMode(mode: GroupMode) {
        val current = getConfig()
        dao.upsertConfig(current.copy(groupMode = mode.name))
    }

    // ── Contacts ──────────────────────────────────────────────────────────────

    fun observeContacts(): Flow<List<ContactEntity>> = dao.observeContacts()

    suspend fun addContact(name: String) {
        dao.insertContact(ContactEntity(name = name.trim()))
    }

    suspend fun deleteContact(contact: ContactEntity) = dao.deleteContact(contact)

    // ── Groups ────────────────────────────────────────────────────────────────

    fun observeGroups(): Flow<List<GroupEntity>> = dao.observeGroups()

    suspend fun addGroup(name: String) {
        dao.insertGroup(GroupEntity(name = name.trim()))
    }

    suspend fun deleteGroup(group: GroupEntity) = dao.deleteGroup(group)

    // ── Engine gate ───────────────────────────────────────────────────────────

    /**
     * Returns true if auto-reply should proceed for this sender/group combination.
     *
     * @param sender  The notification title (contact name or sender in a group chat)
     * @param groupName  The conversation title when it's a group message; null for DMs
     */
    suspend fun isAllowed(sender: String, groupName: String?): Boolean {
        val config = dao.getConfig() ?: return true

        return if (groupName != null) {
            // Group message path
            if (!config.groupsEnabled) {
                AppLogger.d(TAG, "Groups disabled — blocking group message from '$groupName'")
                return false
            }
            when (safeGroupMode(config.groupMode)) {
                GroupMode.ALL_GROUPS -> true
                GroupMode.MY_GROUP_LIST -> {
                    val groups = dao.getAllGroups()
                    val allowed = groups.any { it.name.equals(groupName.trim(), ignoreCase = true) }
                    AppLogger.d(TAG, "MY_GROUP_LIST check for '$groupName': allowed=$allowed")
                    allowed
                }

                GroupMode.EXCEPT_MY_GROUP_LIST -> {
                    val groups = dao.getAllGroups()
                    val allowed =
                        groups.none { it.name.equals(groupName.trim(), ignoreCase = true) }
                    AppLogger.d(
                        TAG,
                        "EXCEPT_MY_GROUP_LIST check for '$groupName': allowed=$allowed"
                    )
                    allowed
                }
            }
        } else {
            // Direct message path
            when (safeContactMode(config.contactMode)) {
                ContactMode.EVERYONE -> true
                ContactMode.MY_LIST -> {
                    val contacts = dao.getAllContacts()
                    val allowed = contacts.any { it.name.equals(sender.trim(), ignoreCase = true) }
                    AppLogger.d(TAG, "MY_LIST check for '$sender': allowed=$allowed")
                    allowed
                }

                ContactMode.EXCEPT_MY_LIST -> {
                    val contacts = dao.getAllContacts()
                    val allowed = contacts.none { it.name.equals(sender.trim(), ignoreCase = true) }
                    AppLogger.d(TAG, "EXCEPT_MY_LIST check for '$sender': allowed=$allowed")
                    allowed
                }

                ContactMode.EXCEPT_PHONE_CONTACTS -> {
                    val isPhone = isPhoneContact(sender)
                    AppLogger.d(
                        TAG,
                        "EXCEPT_PHONE_CONTACTS check for '$sender': isPhoneContact=$isPhone"
                    )
                    !isPhone
                }
            }
        }
    }

    private fun isPhoneContact(name: String): Boolean {
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                "${ContactsContract.Contacts.DISPLAY_NAME} = ?",
                arrayOf(name.trim()),
                null,
            )
            cursor?.use { it.count > 0 } ?: false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Phone contacts check failed: ${e.message}")
            false
        }
    }

    private fun safeContactMode(value: String): ContactMode =
        runCatching { ContactMode.valueOf(value) }.getOrDefault(ContactMode.EVERYONE)

    private fun safeGroupMode(value: String): GroupMode =
        runCatching { GroupMode.valueOf(value) }.getOrDefault(GroupMode.ALL_GROUPS)
}
