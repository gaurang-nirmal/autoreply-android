package com.psspl.autoreply.ui.screens.replynotifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
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
    val isEmpty: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ReplyNotificationsViewModel @Inject constructor(
    private val repository: ReplyNotificationsRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { appSettingsRepository.markNotificationsViewed() }
    }

    val uiState = repository.allNotifications
        .map { notifications ->
            if (notifications.isEmpty()) {
                ReplyNotificationsUiState(isEmpty = true, isLoading = false)
            } else {
                val items = buildGroupedList(notifications)
                ReplyNotificationsUiState(items = items, isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReplyNotificationsUiState(),
        )

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

    companion object {
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
            "com.viber.voip" -> "Viber"
            else -> packageName
        }

        private fun isSameDay(a: Calendar, b: Calendar): Boolean =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
