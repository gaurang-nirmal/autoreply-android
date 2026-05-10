package com.psspl.autoreply.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.data.auth.AuthRepository
import com.psspl.autoreply.database.entity.AppSettingsEntity
import com.psspl.autoreply.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val accountType: String = "Free",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepository: AuthRepository,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState = authRepository.currentUser
        .map { user ->
            SettingsUiState(
                displayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User",
                email = user?.email ?: "",
                photoUrl = user?.photoUrl,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    val appSettings = appSettingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null as AppSettingsEntity?,
        )
}
