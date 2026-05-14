package com.psspl.autoreply.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.network.model.AiConfigBody
import com.psspl.autoreply.data.network.model.AiParamSpec
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

data class AiParametersUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasConfig: Boolean = false,
    val provider: String = "",
    val providerLabel: String = "",
    val model: String = "",
    val availableModels: List<String> = emptyList(),
    val temperature: Double = 0.7,
    val maxTokens: Int = 1024,
    val extraParams: Map<String, Double> = emptyMap(),
    val paramSpecs: List<AiParamSpec> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class AiParametersViewModel @Inject constructor(
    private val repository: AiConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiParametersUiState())
    val uiState: StateFlow<AiParametersUiState> = _uiState.asStateFlow()

    fun init(appId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val configResult = repository.getConfig(appId)
            val providersResult = repository.getProviders()

            val config = (configResult as? Result.Success)?.data
            val providers = (providersResult as? Result.Success)?.data ?: emptyList()

            if (config != null) {
                val meta = providers.firstOrNull { it.id == config.provider }
                val extraParamsMap = config.config.extraParams
                    ?.mapValues { (_, v) -> (v as? Number)?.toDouble() ?: 0.0 }
                    ?: emptyMap()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasConfig = true,
                        provider = config.provider,
                        providerLabel = meta?.label ?: config.provider,
                        model = config.config.model,
                        availableModels = meta?.availableModels ?: emptyList(),
                        temperature = config.config.temperature ?: 0.7,
                        maxTokens = config.config.maxTokens ?: 1024,
                        extraParams = extraParamsMap,
                        paramSpecs = meta?.extraParams ?: emptyList(),
                    )
                }
            } else {
                val isNotFound = (configResult as? Result.Error)?.message == "NOT_FOUND"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (isNotFound) null else (configResult as? Result.Error)?.message,
                    )
                }
            }
        }
    }

    fun updateModel(appId: Int, model: String) {
        _uiState.update { it.copy(model = model) }
        save(appId)
    }

    fun updateTemperature(appId: Int, temperature: Double) {
        _uiState.update { it.copy(temperature = temperature) }
        save(appId)
    }

    fun updateMaxTokens(appId: Int, maxTokens: Int) {
        _uiState.update { it.copy(maxTokens = maxTokens) }
        save(appId)
    }

    fun updateExtraParam(appId: Int, key: String, value: Double) {
        _uiState.update { it.copy(extraParams = it.extraParams + (key to value)) }
        save(appId)
    }

    private fun save(appId: Int) {
        val state = _uiState.value
        if (!state.hasConfig) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val request = UpsertAiConfigRequest(
                provider = state.provider,
                config = AiConfigBody(
                    model = state.model,
                    temperature = state.temperature,
                    maxTokens = state.maxTokens,
                    extraParams = state.extraParams.ifEmpty { null },
                ),
            )
            when (val result = repository.saveConfig(appId, request)) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false) }
                is Result.Error -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
