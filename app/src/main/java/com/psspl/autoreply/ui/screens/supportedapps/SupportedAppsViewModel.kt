package com.psspl.autoreply.ui.screens.supportedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.SupportedAppEntity
import com.psspl.autoreply.repository.SupportedAppsRepository
import com.psspl.autoreply.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportedAppsUiState(
    val apps: List<SupportedAppEntity> = emptyList(),
    val enabledCount: Int = 0,
    val isAtLimit: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class SupportedAppsViewModel @Inject constructor(
    private val repository: SupportedAppsRepository,
) : ViewModel() {

    val uiState = repository.allApps
        .map { apps ->
            val enabledCount = apps.count { it.isEnabled }
            SupportedAppsUiState(
                apps = apps,
                enabledCount = enabledCount,
                isAtLimit = enabledCount >= AppConstants.MAX_FREE_APPS,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SupportedAppsUiState(),
        )

    init {
        viewModelScope.launch {
            repository.seedDefaultAppsIfEmpty(defaultApps)
        }
    }

    fun toggleApp(app: SupportedAppEntity) {
        // Prevent enabling when free plan limit is reached
        if (!app.isEnabled && uiState.value.isAtLimit) return
        viewModelScope.launch {
            repository.update(app.copy(isEnabled = !app.isEnabled))
        }
    }

    companion object {
        val defaultApps = listOf(
            SupportedAppEntity(appPackage = "com.whatsapp", displayName = "WhatsApp"),
            SupportedAppEntity(appPackage = "com.whatsapp.w4b", displayName = "WhatsApp Business"),
            SupportedAppEntity(appPackage = "org.telegram.messenger", displayName = "Telegram"),
            SupportedAppEntity(appPackage = "com.facebook.orca", displayName = "Messenger"),
            SupportedAppEntity(appPackage = "com.instagram.android", displayName = "Instagram"),
            SupportedAppEntity(appPackage = "com.twitter.android", displayName = "Twitter / X"),
            SupportedAppEntity(appPackage = "com.linkedin.android", displayName = "LinkedIn"),
            SupportedAppEntity(appPackage = "org.thoughtcrime.securesms", displayName = "Signal"),
            SupportedAppEntity(
                appPackage = "com.facebook.pages.app",
                displayName = "Meta Business Suite"
            ),
            SupportedAppEntity(appPackage = "com.viber.voip", displayName = "Viber"),
        )
    }
}
