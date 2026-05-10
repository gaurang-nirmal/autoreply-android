package com.psspl.autoreply.ui.screens.autoreplyconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoReplyConfigViewModel @Inject constructor(
    private val repo: AppSettingsRepository,
) : ViewModel() {

    val autoReplyMessage = repo.settings
        .map { it?.autoReplyMessage ?: DEFAULT_MESSAGE }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DEFAULT_MESSAGE,
        )

    val replyType = repo.settings
        .map { entity ->
            ReplyType.entries.find { it.name == entity?.replyType } ?: ReplyType.CUSTOM
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReplyType.CUSTOM,
        )

    fun setMessage(message: String) = viewModelScope.launch {
        repo.setAutoReplyMessage(message)
    }

    fun setReplyType(type: ReplyType) = viewModelScope.launch {
        repo.setReplyType(type.name)
    }

    companion object {
        const val DEFAULT_MESSAGE = "I am sleeping, text you later."
    }
}
