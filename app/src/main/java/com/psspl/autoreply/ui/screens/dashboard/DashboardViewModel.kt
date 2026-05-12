package com.psspl.autoreply.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.DefaultMessageEntity
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.DefaultMessageRepository
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import com.psspl.autoreply.repository.SupportedAppsRepository
import com.psspl.autoreply.ui.screens.autoreplyconfig.ReplyType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val defaultMessageRepository: DefaultMessageRepository,
    replyNotificationsRepository: ReplyNotificationsRepository,
    supportedAppsRepository: SupportedAppsRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { defaultMessageRepository.seedIfEmpty() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationCount = appSettingsRepository.settings
        .map { it?.notificationsLastViewedAt ?: 0L }
        .flatMapLatest { lastViewed -> replyNotificationsRepository.countUnread(lastViewed) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    val isAutoReplyEnabled = appSettingsRepository.isAutoReplyEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val autoReplyMessage = appSettingsRepository.settings
        .map { it?.autoReplyMessage ?: DEFAULT_MESSAGE }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_MESSAGE,
        )

    val selectedReplyType = appSettingsRepository.settings
        .map { settings ->
            val key = settings?.replyType ?: ReplyType.CUSTOM.name
            runCatching { ReplyType.valueOf(key) }.getOrDefault(ReplyType.CUSTOM)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReplyType.CUSTOM,
        )

    val sentRepliesCount = replyNotificationsRepository.allNotifications
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    val enabledAppsCount = supportedAppsRepository.enabledApps
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    val defaultMessages = defaultMessageRepository.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val messagesExpanded = appSettingsRepository.settings
        .map { it?.messagesExpanded ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    fun toggleAutoReply(enabled: Boolean) = viewModelScope.launch {
        appSettingsRepository.setAutoReplyEnabled(enabled)
    }

    fun toggleMessagesExpanded() = viewModelScope.launch {
        appSettingsRepository.setMessagesExpanded(!messagesExpanded.value)
    }

    fun selectMessage(message: DefaultMessageEntity) = viewModelScope.launch {
        appSettingsRepository.setAutoReplyMessage(message.message)
        appSettingsRepository.setReplyType(ReplyType.CUSTOM.name)
    }

    fun clearCustomMessages() = viewModelScope.launch {
        defaultMessageRepository.clearCustomMessages()
    }

    companion object {
        private const val DEFAULT_MESSAGE = "I am sleeping, text you later."
    }
}
