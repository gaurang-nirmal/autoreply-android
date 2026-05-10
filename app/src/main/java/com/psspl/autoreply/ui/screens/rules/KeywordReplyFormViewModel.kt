package com.psspl.autoreply.ui.screens.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.repository.KeywordRuleRepository
import com.psspl.autoreply.utils.MatchType
import com.psspl.autoreply.utils.ReplyOption
import com.psspl.autoreply.utils.toDbString
import com.psspl.autoreply.utils.toReplyOptionSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KeywordReplyFormState(
    val keyword: String = "",
    val replyText: String = "",
    val matchType: MatchType = MatchType.EXACT,
    val selectedReplyOptions: Set<ReplyOption> = emptySet(),
    val sendEmail: Boolean = false,
    val isActive: Boolean = true,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class KeywordReplyFormViewModel @Inject constructor(
    private val keywordRuleRepository: KeywordRuleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(KeywordReplyFormState())
    val state: StateFlow<KeywordReplyFormState> = _state.asStateFlow()

    /** Load an existing rule for editing. Call this once when the screen opens with a ruleId. */
    fun loadRule(ruleId: Int) {
        if (ruleId <= 0) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            keywordRuleRepository.getById(ruleId).collect { entity ->
                if (entity != null) {
                    _state.update {
                        it.copy(
                            keyword = entity.keyword,
                            replyText = entity.replyText,
                            matchType = MatchType.entries.find { m -> m.name == entity.matchType }
                                ?: MatchType.EXACT,
                            selectedReplyOptions = entity.replyOptions.toReplyOptionSet(),
                            sendEmail = entity.sendEmail,
                            isActive = entity.isActive,
                            isLoading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onKeywordChange(value: String) =
        _state.update { it.copy(keyword = value, errorMessage = null) }

    fun onReplyTextChange(value: String) = _state.update { it.copy(replyText = value) }

    /** Insert a tag placeholder at the end of the current reply text. */
    fun insertTag(tag: String) {
        val current = _state.value.replyText
        val insertion = "{$tag}"
        _state.update { it.copy(replyText = if (current.isEmpty()) insertion else "$current $insertion") }
    }

    fun onMatchTypeChange(type: MatchType) = _state.update { it.copy(matchType = type) }

    fun onReplyOptionToggled(option: ReplyOption) {
        _state.update { s ->
            val updated = if (option in s.selectedReplyOptions)
                s.selectedReplyOptions - option
            else
                s.selectedReplyOptions + option
            s.copy(selectedReplyOptions = updated)
        }
    }

    fun onSendEmailChange(value: Boolean) = _state.update { it.copy(sendEmail = value) }

    fun onActiveChange(value: Boolean) = _state.update { it.copy(isActive = value) }

    /** Save (insert or update). [ruleId] == 0 means new rule. */
    fun saveRule(ruleId: Int) {
        val s = _state.value
        if (s.keyword.isBlank()) {
            _state.update { it.copy(errorMessage = "Keyword cannot be empty") }
            return
        }
        if (s.replyText.isBlank()) {
            _state.update { it.copy(errorMessage = "Reply message cannot be empty") }
            return
        }
        viewModelScope.launch {
            val entity = KeywordRuleEntity(
                id = ruleId,
                keyword = s.keyword.trim(),
                replyText = s.replyText.trim(),
                matchType = s.matchType.name,
                replyOptions = s.selectedReplyOptions.toDbString(),
                sendEmail = s.sendEmail,
                isActive = s.isActive,
            )
            if (ruleId == 0) {
                keywordRuleRepository.insert(entity)
            } else {
                keywordRuleRepository.update(entity)
            }
            _state.update { it.copy(isSaved = true) }
        }
    }
}
