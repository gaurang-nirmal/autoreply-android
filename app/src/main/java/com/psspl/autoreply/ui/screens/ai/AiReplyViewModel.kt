package com.psspl.autoreply.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.network.model.AiConfigBody
import com.psspl.autoreply.data.network.model.AiProviderMeta
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

data class AiReplyUiState(
    val providers: List<AiProviderMeta> = emptyList(),
    val selectedProviderId: String = "openai",
    val apiKey: String = "",
    val selectedModel: String = "",
    val isApiKeyVisible: Boolean = false,
    val isModelDropdownExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasExistingConfig: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
) {
    val selectedProvider: AiProviderMeta?
        get() = providers.firstOrNull { it.id == selectedProviderId }
}

@HiltViewModel
class AiReplyViewModel @Inject constructor(
    private val repository: AiConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiReplyUiState())
    val uiState: StateFlow<AiReplyUiState> = _uiState.asStateFlow()

    fun init(appId: Int) {
        loadProviders(appId)
    }

    private fun loadProviders(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val providersResult = repository.getProviders()
            if (providersResult is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = providersResult.message) }
                return@launch
            }
            val providers = (providersResult as Result.Success).data

            // Load existing saved config (404 is fine — means first-time setup)
            val configResult = repository.getConfig(appId)
            val savedConfig = if (configResult is Result.Success) configResult.data else null
            val hasConfig = savedConfig != null

            val currentProviderId = savedConfig?.provider ?: providers.firstOrNull()?.id ?: "openai"
            val currentProvider = providers.firstOrNull { it.id == currentProviderId }
            val currentModel = savedConfig?.config?.model ?: currentProvider?.defaultModel ?: ""

            _uiState.update {
                it.copy(
                    isLoading = false,
                    providers = providers,
                    selectedProviderId = currentProviderId,
                    selectedModel = currentModel,
                    hasExistingConfig = hasConfig,
                )
            }
        }
    }

    fun selectProvider(providerId: String) {
        val provider = _uiState.value.providers.firstOrNull { it.id == providerId } ?: return
        _uiState.update {
            it.copy(
                selectedProviderId = providerId,
                selectedModel = provider.defaultModel,
                apiKey = "",
            )
        }
    }

    fun onApiKeyChange(value: String) = _uiState.update { it.copy(apiKey = value) }

    fun toggleApiKeyVisibility() =
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }

    fun onModelSelected(model: String) = _uiState.update {
        it.copy(selectedModel = model, isModelDropdownExpanded = false)
    }

    fun toggleModelDropdown(expanded: Boolean) = _uiState.update {
        it.copy(isModelDropdownExpanded = expanded)
    }

    fun save(appId: Int) {
        val state = _uiState.value
        if (state.selectedModel.isBlank()) return
        if (!state.hasExistingConfig && state.apiKey.isBlank()) {
            _uiState.update { it.copy(error = "API key is required for first-time setup") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }

            val request = UpsertAiConfigRequest(
                provider = state.selectedProviderId,
                apiKey = state.apiKey.ifBlank { null },
                config = AiConfigBody(model = state.selectedModel),
            )

            when (val result = repository.saveConfig(appId, request)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true, hasExistingConfig = true)
                }

                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun reset(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.deleteConfig(appId)) {
                is Result.Success -> {
                    val defaultProvider = _uiState.value.providers.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            apiKey = "",
                            selectedProviderId = defaultProvider?.id ?: "openai",
                            selectedModel = defaultProvider?.defaultModel ?: "",
                            hasExistingConfig = false,
                            saveSuccess = false,
                        )
                    }
                }

                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false) }
}
