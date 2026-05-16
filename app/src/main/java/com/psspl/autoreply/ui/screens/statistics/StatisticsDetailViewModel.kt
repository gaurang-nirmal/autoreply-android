package com.psspl.autoreply.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.ReplyNotificationEntity
import com.psspl.autoreply.repository.ReplyNotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StatisticsDetailUiState(
    val replyText: String = "",
    val sends: List<ReplyNotificationEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatisticsDetailViewModel @Inject constructor(
    private val repository: ReplyNotificationsRepository,
) : ViewModel() {

    private val _replyText = MutableStateFlow("")

    val uiState = combine(
        _replyText.flatMapLatest { text ->
            repository.getByReplyText(text)
        },
        _replyText,
    ) { sends, replyText ->
        StatisticsDetailUiState(
            replyText = replyText,
            sends = sends,
            isLoading = replyText.isEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsDetailUiState(),
    )

    fun setReplyText(replyText: String) {
        if (_replyText.value != replyText) {
            _replyText.value = replyText
        }
    }

    fun exportCsv(): String {
        val sends = uiState.value.sends
        val sb = StringBuilder()
        sb.appendLine("Sender,App,Date,Time")
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sends.forEach { entity ->
            val date = dateFmt.format(Date(entity.timestamp))
            val time = timeFmt.format(Date(entity.timestamp))
            val sender = entity.senderName.replace("\"", "\"\"")
            sb.appendLine("\"$sender\",${entity.appPackage},$date,$time")
        }
        return sb.toString()
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
            "com.whatsapp" -> "WhatsApp"
            "com.whatsapp.w4b" -> "WA Business"
            "org.telegram.messenger" -> "Telegram"
            "com.facebook.orca" -> "Messenger"
            "com.facebook.mlite" -> "Messenger Lite"
            "com.instagram.android" -> "Instagram"
            "com.twitter.android" -> "Twitter / X"
            "com.linkedin.android" -> "LinkedIn"
            "org.thoughtcrime.securesms" -> "Signal"
            "com.facebook.pages.app" -> "Meta Business"
            "com.viber.voip" -> "Viber"
            else -> packageName.substringAfterLast(".")
        }

        private fun isSameDay(a: Calendar, b: Calendar): Boolean =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
