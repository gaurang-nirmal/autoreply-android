package com.psspl.autoreply.ui.screens.replytiming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.ReplyLimitTrackingEntity
import com.psspl.autoreply.database.entity.ReplyTimingConfigEntity
import com.psspl.autoreply.repository.ReplyTimingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReplyTimingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReplyTimingRepository,
) : ViewModel() {

    /** e.g. "keyword", "menu" — injected automatically from the nav back-stack entry */
    val replyType: String = checkNotNull(savedStateHandle["replyType"])

    // ─── Config ───────────────────────────────────────────────────────────────

    val config: StateFlow<ReplyTimingConfigEntity> = repository.getConfig(replyType)
        .map { it ?: ReplyTimingConfigEntity(replyType = replyType) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReplyTimingConfigEntity(replyType = replyType),
        )

    // ─── Limit tracking list ──────────────────────────────────────────────────

    val limitTracking: StateFlow<List<ReplyLimitTrackingEntity>> =
        repository.getTrackingForType(replyType)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun update(transform: ReplyTimingConfigEntity.() -> ReplyTimingConfigEntity) {
        viewModelScope.launch { repository.upsertConfig(config.value.transform()) }
    }

    // ─── Config actions ───────────────────────────────────────────────────────

    fun setReplyMode(mode: ReplyMode) = update { copy(replyMode = mode.name) }

    fun setWaitSeconds(seconds: Int) = update { copy(waitSeconds = seconds.coerceIn(1, 86_400)) }

    fun setDelaySeconds(seconds: Int) = update { copy(delaySeconds = seconds.coerceIn(1, 86_400)) }

    fun setReplyLimitEnabled(enabled: Boolean) = update { copy(replyLimitEnabled = enabled) }

    fun setMaxReplies(count: Int) = update { copy(maxReplies = count.coerceIn(1, 10_000)) }

    // ─── Tracking actions ─────────────────────────────────────────────────────

    fun clearAllTracking() {
        viewModelScope.launch { repository.clearTrackingForType(replyType) }
    }

    fun clearContactTracking(contactKey: String) {
        viewModelScope.launch { repository.clearTrackingForContact(replyType, contactKey) }
    }
}
