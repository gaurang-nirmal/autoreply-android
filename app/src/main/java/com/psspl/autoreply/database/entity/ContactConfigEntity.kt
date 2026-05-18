package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.psspl.autoreply.utils.ContactMode
import com.psspl.autoreply.utils.GroupMode

@Entity(tableName = "contact_config")
data class ContactConfigEntity(
    @PrimaryKey val id: Int = 1,

    @ColumnInfo(name = "contact_mode")
    val contactMode: String = ContactMode.EVERYONE.name,

    @ColumnInfo(name = "groups_enabled")
    val groupsEnabled: Boolean = false,

    @ColumnInfo(name = "group_mode")
    val groupMode: String = GroupMode.ALL_GROUPS.name,
)
