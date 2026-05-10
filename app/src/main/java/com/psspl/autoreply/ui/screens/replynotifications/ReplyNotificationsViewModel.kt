package com.psspl.autoreply.ui.screens.replynotifications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import com.psspl.autoreply.repository.SupportedAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class ReplyNotificationListItem {
    data class Header(val date: String) : ReplyNotificationListItem()
    data class Entry(val entity: ReplyNotificationEntity) : ReplyNotificationListItem()
}

data class ReplyNotificationsUiState(
    val items: List<ReplyNotificationListItem> = emptyList(),
    val filteredNotifications: List<ReplyNotificationEntity> = emptyList(),
    val appOptions: List<ReplyNotificationAppOption> = emptyList(),
    val appliedFilter: ReplyNotificationFilter = ReplyNotificationFilter(),
    val draftFilter: ReplyNotificationFilter = ReplyNotificationFilter(),
    val totalCount: Int = 0,
    val filteredCount: Int = 0,
    val draftFilteredCount: Int = 0,
    val isFilterSheetVisible: Boolean = false,
    val isFilterActive: Boolean = false,
    val isEmpty: Boolean = false,
    val isFilteredEmpty: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ReplyNotificationsViewModel @Inject constructor(
    private val repository: ReplyNotificationsRepository,
    private val appSettingsRepository: AppSettingsRepository,
    supportedAppsRepository: SupportedAppsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _appliedFilter = MutableStateFlow(savedStateHandle.toReplyNotificationFilter())
    private val _draftFilter = MutableStateFlow(_appliedFilter.value)
    private val _isFilterSheetVisible = MutableStateFlow(false)

    init {
        viewModelScope.launch { appSettingsRepository.markNotificationsViewed() }
    }

    val uiState = combine(
        repository.allNotifications,
        supportedAppsRepository.allApps,
        _appliedFilter,
        _draftFilter,
        _isFilterSheetVisible,
    ) { notifications, supportedApps, appliedFilter, draftFilter, isFilterSheetVisible ->
        val appOptions = supportedApps
            .map { it.toReplyNotificationAppOption() }
            .withDefaultAppOptions()

        val filteredNotifications = notifications.filter { it.matches(appliedFilter) }
        val draftFilteredCount = notifications.count { it.matches(draftFilter) }
        val items = buildGroupedList(filteredNotifications)

        ReplyNotificationsUiState(
            items = items,
            filteredNotifications = filteredNotifications,
            appOptions = appOptions,
            appliedFilter = appliedFilter,
            draftFilter = draftFilter,
            totalCount = notifications.size,
            filteredCount = filteredNotifications.size,
            draftFilteredCount = draftFilteredCount,
            isFilterSheetVisible = isFilterSheetVisible,
            isFilterActive = !appliedFilter.isDefault,
            isEmpty = notifications.isEmpty(),
            isFilteredEmpty = notifications.isNotEmpty() && filteredNotifications.isEmpty(),
            isLoading = false,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReplyNotificationsUiState(),
        )

    fun showFilters() {
        _draftFilter.value = _appliedFilter.value
        _isFilterSheetVisible.value = true
    }

    fun hideFilters() {
        _isFilterSheetVisible.value = false
    }

    fun updateDateFilter(dateFilter: ReplyNotificationDateFilter) {
        _draftFilter.value = _draftFilter.value.copy(dateFilter = dateFilter)
    }

    fun updateAppFilter(appPackage: String?) {
        _draftFilter.value = _draftFilter.value.copy(appPackage = appPackage)
    }

    fun updateContactQuery(contactQuery: String) {
        _draftFilter.value = _draftFilter.value.copy(contactQuery = contactQuery)
    }

    fun updateMessageQuery(messageQuery: String) {
        _draftFilter.value = _draftFilter.value.copy(messageQuery = messageQuery)
    }

    fun applyFilters() {
        val filter = _draftFilter.value
        _appliedFilter.value = filter
        savedStateHandle.save(filter)
        _isFilterSheetVisible.value = false
    }

    fun resetFilters() {
        _draftFilter.value = ReplyNotificationFilter()
        _appliedFilter.value = ReplyNotificationFilter()
        savedStateHandle.save(ReplyNotificationFilter())
    }

    fun exportText(): String =
        ReplyNotificationExportFormatter.format(uiState.value.filteredNotifications)

    fun clearHistory() {
        viewModelScope.launch { repository.deleteAll() }
    }

    private fun buildGroupedList(
        notifications: List<ReplyNotificationEntity>,
    ): List<ReplyNotificationListItem> {
        val result = mutableListOf<ReplyNotificationListItem>()
        var lastLabel = ""
        notifications.forEach { entity ->
            val label = entity.timestamp.toDateLabel()
            if (label != lastLabel) {
                result += ReplyNotificationListItem.Header(label)
                lastLabel = label
            }
            result += ReplyNotificationListItem.Entry(entity)
        }
        return result
    }

    private fun ReplyNotificationEntity.matches(filter: ReplyNotificationFilter): Boolean =
        matchesDate(filter.dateFilter) &&
                (filter.appPackage == null || appPackage == filter.appPackage) &&
                senderName.contains(filter.contactQuery.trim(), ignoreCase = true) &&
                replyText.contains(filter.messageQuery.trim(), ignoreCase = true)

    private fun ReplyNotificationEntity.matchesDate(filter: ReplyNotificationDateFilter): Boolean {
        val now = Calendar.getInstance()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return when (filter) {
            ReplyNotificationDateFilter.All -> true
            ReplyNotificationDateFilter.Today -> timestamp >= todayStart.timeInMillis
            ReplyNotificationDateFilter.Yesterday -> {
                val yesterdayStart = todayStart.copy().apply { add(Calendar.DAY_OF_YEAR, -1) }
                timestamp >= yesterdayStart.timeInMillis && timestamp < todayStart.timeInMillis
            }

            ReplyNotificationDateFilter.Last7Days -> {
                val start = todayStart.copy().apply { add(Calendar.DAY_OF_YEAR, -6) }
                timestamp >= start.timeInMillis && timestamp <= now.timeInMillis
            }

            ReplyNotificationDateFilter.Last30Days -> {
                val start = todayStart.copy().apply { add(Calendar.DAY_OF_YEAR, -29) }
                timestamp >= start.timeInMillis && timestamp <= now.timeInMillis
            }
        }
    }

    private fun Calendar.copy(): Calendar =
        (clone() as Calendar)

    companion object {
        private const val KEY_DATE_FILTER = "reply_notifications_date_filter"
        private const val KEY_APP_PACKAGE = "reply_notifications_app_package"
        private const val KEY_CONTACT_QUERY = "reply_notifications_contact_query"
        private const val KEY_MESSAGE_QUERY = "reply_notifications_message_query"

        private val defaultAppOptions = listOf(
            ReplyNotificationAppOption("com.whatsapp", "WhatsApp"),
            ReplyNotificationAppOption("com.whatsapp.w4b", "WhatsApp Business"),
            ReplyNotificationAppOption("org.telegram.messenger", "Telegram"),
            ReplyNotificationAppOption("com.facebook.orca", "Messenger"),
            ReplyNotificationAppOption("com.facebook.mlite", "Messenger Lite"),
            ReplyNotificationAppOption("com.instagram.android", "Instagram"),
            ReplyNotificationAppOption("com.twitter.android", "Twitter / X"),
            ReplyNotificationAppOption("com.linkedin.android", "LinkedIn"),
            ReplyNotificationAppOption("org.thoughtcrime.securesms", "Signal"),
            ReplyNotificationAppOption("com.facebook.pages.app", "Meta Business Suite"),
            ReplyNotificationAppOption("com.viber.voip", "Viber"),
        )

        private fun List<ReplyNotificationAppOption>.withDefaultAppOptions(): List<ReplyNotificationAppOption> {
            val existingByPackage = associateBy { it.appPackage }
            val defaultsAndExisting = defaultAppOptions.map { default ->
                existingByPackage[default.appPackage] ?: default
            }
            val customOptions = filterNot { option ->
                defaultAppOptions.any { it.appPackage == option.appPackage }
            }
            return (defaultsAndExisting + customOptions).sortedBy { it.displayName }
        }

        private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun Long.toDateLabel(): String {
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val target = Calendar.getInstance().apply { timeInMillis = this@toDateLabel }
            return when {
                isSameDay(target, today) -> "Today"
                isSameDay(target, yesterday) -> "Yesterday"
                else -> dateFormat.format(Date(this))
            }
        }

        fun Long.toTimeLabel(): String = timeFormat.format(Date(this))

        fun appDisplayName(packageName: String): String = when (packageName) {
            "com.facebook.orca" -> "Messenger"
            "com.whatsapp" -> "WhatsApp"
            "com.whatsapp.w4b" -> "WhatsApp Business"
            "org.telegram.messenger" -> "Telegram"
            "com.instagram.android" -> "Instagram"
            "com.twitter.android" -> "Twitter / X"
            "com.linkedin.android" -> "LinkedIn"
            "org.thoughtcrime.securesms" -> "Signal"
            "com.facebook.pages.app" -> "Meta Business Suite"
            "com.facebook.mlite" -> "Messenger Lite"
            "com.viber.voip" -> "Viber"
            else -> packageName
        }

        private fun isSameDay(a: Calendar, b: Calendar): Boolean =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

        private fun SavedStateHandle.toReplyNotificationFilter(): ReplyNotificationFilter {
            val dateFilterName = get<String>(KEY_DATE_FILTER)
            val dateFilter = ReplyNotificationDateFilter.entries
                .firstOrNull { it.name == dateFilterName }
                ?: ReplyNotificationDateFilter.All

            return ReplyNotificationFilter(
                dateFilter = dateFilter,
                appPackage = get<String>(KEY_APP_PACKAGE),
                contactQuery = get<String>(KEY_CONTACT_QUERY).orEmpty(),
                messageQuery = get<String>(KEY_MESSAGE_QUERY).orEmpty(),
            )
        }

        private fun SavedStateHandle.save(filter: ReplyNotificationFilter) {
            this[KEY_DATE_FILTER] = filter.dateFilter.name
            this[KEY_APP_PACKAGE] = filter.appPackage
            this[KEY_CONTACT_QUERY] = filter.contactQuery
            this[KEY_MESSAGE_QUERY] = filter.messageQuery
        }
    }
}
