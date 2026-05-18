package com.psspl.autoreply.ui.screens.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.auth.AuthRepository
import com.psspl.autoreply.data.auth.model.AuthUser
import com.psspl.autoreply.data.network.ApiService
import com.psspl.autoreply.data.network.model.ContactRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ContactUiState {
    object Idle : ContactUiState()
    object Loading : ContactUiState()
    data class Success(val ticketId: String?) : ContactUiState()
    data class Error(val message: String) : ContactUiState()
}

sealed class DeleteAccountUiState {
    object Idle : DeleteAccountUiState()
    object Loading : DeleteAccountUiState()
    object Success : DeleteAccountUiState()
    data class Error(val message: String) : DeleteAccountUiState()
}

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apiService: ApiService,
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _contactState = MutableStateFlow<ContactUiState>(ContactUiState.Idle)
    val contactState: StateFlow<ContactUiState> = _contactState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteAccountUiState>(DeleteAccountUiState.Idle)
    val deleteState: StateFlow<DeleteAccountUiState> = _deleteState.asStateFlow()

    fun submitContact(
        subject: String,
        message: String,
        attachmentBase64: String?,
        attachmentName: String?
    ) {
        val email = currentUser.value?.email ?: return
        viewModelScope.launch {
            _contactState.value = ContactUiState.Loading
            _contactState.value = try {
                val response = apiService.submitContactRequest(
                    ContactRequest(
                        email = email,
                        subject = subject,
                        message = message,
                        attachmentBase64 = attachmentBase64,
                        attachmentName = attachmentName,
                    )
                )
                if (response.isSuccessful) {
                    ContactUiState.Success(response.body()?.ticketId)
                } else {
                    ContactUiState.Error("Server error (${response.code()}). Please try again.")
                }
            } catch (e: Exception) {
                ContactUiState.Error(e.message ?: "Failed to send. Check your connection.")
            }
        }
    }

    fun resetContactState() {
        _contactState.value = ContactUiState.Idle
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _deleteState.value = DeleteAccountUiState.Loading
            _deleteState.value = authRepository.deleteAccount().fold(
                onSuccess = { DeleteAccountUiState.Success },
                onFailure = { DeleteAccountUiState.Error(it.message ?: "Deletion failed.") },
            )
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteAccountUiState.Idle
    }
}
