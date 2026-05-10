package com.psspl.autoreply.ui.screens.replynotifications

import com.psspl.autoreply.database.entity.SupportedAppEntity

enum class ReplyNotificationDateFilter(val label: String) {
    All("Show All"),
    Today("Today"),
    Yesterday("Yesterday"),
    Last7Days("Last 7 Days"),
    Last30Days("Last 30 Days"),
}

data class ReplyNotificationAppOption(
    val appPackage: String,
    val displayName: String,
)

data class ReplyNotificationFilter(
    val dateFilter: ReplyNotificationDateFilter = ReplyNotificationDateFilter.All,
    val appPackage: String? = null,
    val contactQuery: String = "",
    val messageQuery: String = "",
) {
    val isDefault: Boolean
        get() = dateFilter == ReplyNotificationDateFilter.All &&
                appPackage == null &&
                contactQuery.isBlank() &&
                messageQuery.isBlank()
}

fun SupportedAppEntity.toReplyNotificationAppOption(): ReplyNotificationAppOption =
    ReplyNotificationAppOption(
        appPackage = appPackage,
        displayName = displayName,
    )
