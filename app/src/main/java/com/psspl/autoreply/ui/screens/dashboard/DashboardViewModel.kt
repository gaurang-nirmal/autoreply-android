package com.psspl.autoreply.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.ReplyNotificationsRepository
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
    replyNotificationsRepository: ReplyNotificationsRepository,
) : ViewModel() {

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

    val sentRepliesCount = replyNotificationsRepository.allNotifications
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    fun toggleAutoReply(enabled: Boolean) = viewModelScope.launch {
        appSettingsRepository.setAutoReplyEnabled(enabled)
    }

    companion object {
        private const val DEFAULT_MESSAGE = "I am sleeping, text you later."
    }
}
