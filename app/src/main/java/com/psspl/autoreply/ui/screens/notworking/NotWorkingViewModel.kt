package com.psspl.autoreply.ui.screens.notworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class ChatEntry {
    data class BotText(val text: String, val id: Long) : ChatEntry()
    data class BotVisual(val id: Long) : ChatEntry()          // notification preview card
    data class UserChoice(val label: String, val id: Long) : ChatEntry()
}

data class NotWorkingUiState(
    val entries: List<ChatEntry> = emptyList(),
    val currentOptions: List<BotOption> = emptyList(),
    val isTyping: Boolean = false,
    val isFinished: Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class NotWorkingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(NotWorkingUiState())
    val state: StateFlow<NotWorkingUiState> = _state.asStateFlow()

    private var idCounter = 0L
    private fun nextId() = idCounter++

    init {
        loadNode(TroubleshootingTree.ROOT_NODE_ID, isFirst = true)
    }

    fun onOptionSelected(option: BotOption) {
        viewModelScope.launch {
            // 1. Record the user's answer as a right-aligned bubble
            appendEntry(ChatEntry.UserChoice(option.label, nextId()))

            // 2. Clear options, show typing indicator
            _state.update { it.copy(currentOptions = emptyList(), isTyping = true) }

            // 3. Typing delay
            delay(700)

            // 4. Navigate to next node or finish
            if (option.nextNodeId == null) {
                _state.update { it.copy(isTyping = false, isFinished = true) }
            } else {
                loadNode(option.nextNodeId, isFirst = false)
            }
        }
    }

    fun restart() {
        idCounter = 0L
        _state.value = NotWorkingUiState()
        loadNode(TroubleshootingTree.ROOT_NODE_ID, isFirst = true)
    }

    private fun loadNode(nodeId: String, isFirst: Boolean) {
        val node = TroubleshootingTree.getNode(nodeId) ?: return

        viewModelScope.launch {
            if (!isFirst) delay(200)

            node.messages.forEachIndexed { index, msg ->
                if (index > 0) {
                    _state.update { it.copy(isTyping = true) }
                    delay(600)
                }
                _state.update { it.copy(isTyping = false) }
                when (msg) {
                    is BotMessage.Text -> appendEntry(ChatEntry.BotText(msg.text, nextId()))
                    is BotMessage.NotificationVisual -> appendEntry(ChatEntry.BotVisual(nextId()))
                }
                delay(150)
            }

            _state.update {
                it.copy(
                    isTyping = false,
                    currentOptions = node.options,
                    isFinished = node.options.isEmpty(),
                )
            }
        }
    }

    private fun appendEntry(entry: ChatEntry) {
        _state.update { it.copy(entries = it.entries + entry) }
    }
}
