package com.psspl.autoreply.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MessageStat(
    val replyText: String,
    val sendCount: Int,
    val contactCount: Int,
)

data class StatisticsUiState(
    val totalCount: Int = 0,
    val appCounts: Map<String, Int> = emptyMap(),
    val messageStats: List<MessageStat> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: ReplyNotificationsRepository,
) : ViewModel() {

    val uiState = repository.allNotifications.map { notifications ->
        val appCounts = notifications
            .groupBy { it.appPackage }
            .mapValues { (_, entries) -> entries.size }

        val messageStats = notifications
            .groupBy { it.replyText }
            .map { (text, entries) ->
                MessageStat(
                    replyText = text,
                    sendCount = entries.size,
                    contactCount = entries.map { it.senderName }.distinct().size,
                )
            }
            .sortedByDescending { it.sendCount }

        StatisticsUiState(
            totalCount = notifications.size,
            appCounts = appCounts,
            messageStats = messageStats,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(),
    )

    fun exportCsv(): String {
        val stats = uiState.value.messageStats
        val sb = StringBuilder()
        sb.appendLine("Reply Message,Send Count,Contact Count")
        stats.forEach { row ->
            val escaped = row.replyText.replace("\"", "\"\"").replace("\n", " ")
            sb.appendLine("\"$escaped\",${row.sendCount},${row.contactCount}")
        }
        return sb.toString()
    }
}
