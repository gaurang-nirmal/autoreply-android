package com.psspl.autoreply.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    val message: String,

    @ColumnInfo(name = "app_package")
    val appPackage: String,

    val timestamp: Long = System.currentTimeMillis()
)
