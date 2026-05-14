package com.psspl.autoreply.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.network.model.BackupPromptItem
import com.psspl.autoreply.data.network.model.TrainingPromptItem
import com.psspl.autoreply.data.repository.AiConfigRepository
import com.psspl.autoreply.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainAiUiState(
    val prompts: List<TrainingPromptItem> = emptyList(),
    val isLoading: Boolean = false,
    val isActionInProgress: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val backupData: List<BackupPromptItem>? = null,
    val promptSaved: Boolean = false,
)

@HiltViewModel
class TrainAiViewModel @Inject constructor(
    private val repository: AiConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainAiUiState())
    val uiState: StateFlow<TrainAiUiState> = _uiState.asStateFlow()

    fun load(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.listPrompts(appId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        prompts = result.data
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun toggleEnabled(appId: Int, promptId: String, enabled: Boolean) {
        // Optimistic update
        _uiState.update { state ->
            state.copy(prompts = state.prompts.map { p ->
                if (p.id == promptId) p.copy(isEnabled = enabled) else p
            })
        }
        viewModelScope.launch {
            when (val result = repository.updatePrompt(appId, promptId, isEnabled = enabled)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(prompts = state.prompts.map { p ->
                        if (p.id == result.data.id) result.data else p
                    })
                }

                is Result.Error -> {
                    // Revert on failure
                    _uiState.update { state ->
                        state.copy(
                            error = result.message,
                            prompts = state.prompts.map { p ->
                                if (p.id == promptId) p.copy(isEnabled = !enabled) else p
                            },
                        )
                    }
                }
            }
        }
    }

    fun deletePrompt(appId: Int, promptId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, error = null) }
            when (val result = repository.deletePrompt(appId, promptId)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        isActionInProgress = false,
                        prompts = state.prompts.filter { it.id != promptId },
                        message = "Prompt deleted",
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isActionInProgress = false, error = result.message)
                }
            }
        }
    }

    fun deleteAll(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, error = null) }
            when (val result = repository.deleteAllPrompts(appId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        prompts = emptyList(),
                        message = "All prompts cleared"
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isActionInProgress = false, error = result.message)
                }
            }
        }
    }

    fun backup(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, error = null) }
            when (val result = repository.backupPrompts(appId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        backupData = result.data.prompts,
                        message = "Backup ready — ${result.data.prompts.size} prompt${if (result.data.prompts.size == 1) "" else "s"}",
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isActionInProgress = false, error = result.message)
                }
            }
        }
    }

    fun restore(appId: Int) {
        val backup = _uiState.value.backupData ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, error = null) }
            when (val result = repository.restorePrompts(appId, backup)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        prompts = result.data,
                        message = "Restored ${result.data.size} prompt${if (result.data.size == 1) "" else "s"}",
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isActionInProgress = false, error = result.message)
                }
            }
        }
    }

    fun createPrompt(appId: Int, content: String, isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, error = null) }
            when (val result = repository.createPrompt(appId, content, isEnabled)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        isActionInProgress = false,
                        prompts = state.prompts + result.data,
                        promptSaved = true,
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isActionInProgress = false, error = result.message)
                }
            }
        }
    }

    fun updatePromptContent(appId: Int, promptId: String, content: String, isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, error = null) }
            when (val result = repository.updatePrompt(
                appId,
                promptId,
                content = content,
                isEnabled = isEnabled
            )) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        isActionInProgress = false,
                        prompts = state.prompts.map { p -> if (p.id == result.data.id) result.data else p },
                        promptSaved = true,
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isActionInProgress = false, error = result.message)
                }
            }
        }
    }

    fun clearPromptSaved() = _uiState.update { it.copy(promptSaved = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
