package com.psspl.autoreply.ui.screens.serverreply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.psspl.autoreply.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Named

data class ServerReplyUiState(
    val url: String = "",
    val headerName: String = "",
    val headerValue: String = "",
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val testResultMessage: String? = null,
    val testResultIsError: Boolean = false,
    val saveSuccess: Boolean = false,
    val showHowItWorksDialog: Boolean = false,
)

@HiltViewModel
class ServerReplyViewModel @Inject constructor(
    private val repo: AppSettingsRepository,
    @Named("Plain") private val okHttpClient: OkHttpClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerReplyUiState())
    val uiState: StateFlow<ServerReplyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.serverReplyUrl,
                repo.serverReplyHeaderName,
                repo.serverReplyHeaderValue,
            ) { url, headerName, headerValue ->
                Triple(url, headerName, headerValue)
            }.collect { (url, headerName, headerValue) ->
                _uiState.update {
                    it.copy(url = url, headerName = headerName, headerValue = headerValue)
                }
            }
        }
    }

    fun onUrlChange(value: String) = _uiState.update { it.copy(url = value) }
    fun onHeaderNameChange(value: String) = _uiState.update { it.copy(headerName = value) }
    fun onHeaderValueChange(value: String) = _uiState.update { it.copy(headerValue = value) }

    fun showHowItWorks() = _uiState.update { it.copy(showHowItWorksDialog = true) }
    fun dismissHowItWorks() = _uiState.update { it.copy(showHowItWorksDialog = false) }

    fun clearTestResult() = _uiState.update { it.copy(testResultMessage = null) }
    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repo.setServerReplyConfig(
                url = state.url.trim(),
                headerName = state.headerName.trim(),
                headerValue = state.headerValue.trim(),
            )
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun sendTestRequest() {
        val url = _uiState.value.url.trim()
        if (url.isBlank()) {
            _uiState.update {
                it.copy(
                    testResultMessage = "Please enter a server URL first.",
                    testResultIsError = true
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResultMessage = null) }

            // Persist config so the notification service can read it from DB
            val state = _uiState.value
            repo.setServerReplyConfig(
                url = state.url.trim(),
                headerName = state.headerName.trim(),
                headerValue = state.headerValue.trim(),
            )

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val json =
                        """{"app":"test","sender":"Test User","message":"This is a test message","group_name":"","phone":""}"""
                    val body = json.toRequestBody("application/json".toMediaType())
                    val requestBuilder = Request.Builder()
                        .url(url)
                        .post(body)
                        .header("Content-Type", "application/json")

                    val state = _uiState.value
                    if (state.headerName.isNotBlank()) {
                        requestBuilder.header(state.headerName.trim(), state.headerValue.trim())
                    }

                    val response = okHttpClient.newCall(requestBuilder.build()).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        return@runCatching Pair("Server returned HTTP ${response.code}.", true)
                    }

                    val reply = runCatching {
                        JsonParser.parseString(responseBody)
                            .asJsonObject
                            .get("reply")
                            ?.takeIf { !it.isJsonNull }
                            ?.asString
                    }.getOrNull()

                    if (reply.isNullOrBlank()) {
                        Pair("Server responded but \"reply\" field was missing or empty.", true)
                    } else {
                        Pair("Server replied: $reply", false)
                    }
                }.getOrElse { e ->
                    Pair("Test failed: ${e.message}", true)
                }
            }

            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResultMessage = result.first,
                    testResultIsError = result.second
                )
            }
        }
    }
}
