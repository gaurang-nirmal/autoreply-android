package com.psspl.autoreply.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.network.model.AiConfigBody
import com.psspl.autoreply.data.network.model.UpsertAiConfigRequest
import com.psspl.autoreply.data.repository.AiConfigRepository
import com.psspl.autoreply.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiSettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val useHistory: Boolean = true,
    val historyTurns: Int = 5,
    val hasConfig: Boolean = false,
    val error: String? = null,
    val clearSuccess: String? = null,
    // Kept internally for PUT calls
    val provider: String = "",
    val model: String = "",
)

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val repository: AiConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    fun init(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getConfig(appId)) {
                is Result.Success -> {
                    val config = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasConfig = true,
                            useHistory = config.useHistory,
                            historyTurns = config.historyTurns,
                            provider = config.provider,
                            model = config.config.model,
                        )
                    }
                }

                is Result.Error -> {
                    // 404 = no config yet; other errors surface to user
                    val isNotFound = result.message == "NOT_FOUND"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = if (isNotFound) null else result.message,
                        )
                    }
                }
            }
        }
    }

    fun toggleUseHistory(appId: Int) {
        val newValue = !_uiState.value.useHistory
        _uiState.update { it.copy(useHistory = newValue) }
        saveHistorySettings(appId)
    }

    fun updateHistoryTurns(appId: Int, turns: Int) {
        _uiState.update { it.copy(historyTurns = turns) }
        saveHistorySettings(appId)
    }

    private fun saveHistorySettings(appId: Int) {
        val state = _uiState.value
        if (!state.hasConfig) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val request = UpsertAiConfigRequest(
                provider = state.provider,
                config = AiConfigBody(model = state.model),
                useHistory = state.useHistory,
                historyTurns = state.historyTurns,
            )
            when (val result = repository.saveConfig(appId, request)) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false) }
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun clearHistory(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, clearSuccess = null) }
            when (val result = repository.clearHistory(appId)) {
                is Result.Success -> _uiState.update {
                    val count = result.data
                    it.copy(
                        isSaving = false,
                        clearSuccess = if (count > 0) "Cleared $count message${if (count == 1) "" else "s"}"
                        else "No history to clear",
                    )
                }

                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(clearSuccess = null) }
}
