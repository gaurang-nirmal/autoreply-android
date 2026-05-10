package com.psspl.autoreply.ui.screens.directmessage

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.DirectMessageEntity
import com.psspl.autoreply.repository.DirectMessageRepository
import com.psspl.autoreply.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WhatsAppTarget(val displayName: String, val packageName: String) {
    WHATSAPP("WhatsApp", "com.whatsapp"),
    WHATSAPP_BUSINESS("WhatsApp Business", "com.whatsapp.w4b"),
}

data class DirectMessageUiState(
    val countryCode: String = "+91",
    val phoneNumber: String = "",
    val message: String = "",
    val selectedApp: WhatsAppTarget = WhatsAppTarget.WHATSAPP,
    val history: List<DirectMessageEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class DirectMessageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DirectMessageRepository,
) : ViewModel() {

    private val _formState = MutableStateFlow(DirectMessageUiState())

    val uiState = combine(_formState, repository.allHistory) { form, history ->
        form.copy(history = history, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DirectMessageUiState(),
    )

    fun onCountryCodeChange(value: String) = _formState.update { it.copy(countryCode = value) }
    fun onPhoneNumberChange(value: String) = _formState.update { it.copy(phoneNumber = value) }
    fun onMessageChange(value: String) = _formState.update { it.copy(message = value) }
    fun onAppSelected(target: WhatsAppTarget) = _formState.update { it.copy(selectedApp = target) }
    fun dismissError() = _formState.update { it.copy(errorMessage = null) }

    fun send() {
        val state = _formState.value
        val digits = "${state.countryCode}${state.phoneNumber}".filter { it.isDigit() }

        if (digits.length < 7) {
            _formState.update { it.copy(errorMessage = "Enter a valid phone number") }
            return
        }

        val uri = if (state.message.isBlank()) {
            Uri.parse("https://wa.me/$digits")
        } else {
            Uri.parse("https://wa.me/$digits?text=${Uri.encode(state.message)}")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(state.selectedApp.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            AppLogger.i(TAG, "Opened ${state.selectedApp.displayName} for $digits")
        } catch (e: ActivityNotFoundException) {
            AppLogger.e(TAG, "${state.selectedApp.displayName} not installed: ${e.message}")
            _formState.update { it.copy(errorMessage = "${state.selectedApp.displayName} is not installed") }
        }

        viewModelScope.launch {
            repository.insert(
                DirectMessageEntity(
                    phoneNumber = "+${digits}",
                    message = state.message,
                    appPackage = state.selectedApp.packageName,
                )
            )
        }
    }

    fun fillFromHistory(entity: DirectMessageEntity) {
        val target = WhatsAppTarget.entries.firstOrNull { it.packageName == entity.appPackage }
            ?: WhatsAppTarget.WHATSAPP
        _formState.update {
            it.copy(
                phoneNumber = entity.phoneNumber.filter { c -> c.isDigit() },
                message = entity.message,
                selectedApp = target,
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.deleteAll() }
    }

    companion object {
        private const val TAG = "DirectMessageVM"
    }
}
