package com.psspl.autoreply.ui.screens.welcomemessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.WelcomeMessageConfigEntity
import com.psspl.autoreply.repository.WelcomeMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DefaultConfig = WelcomeMessageConfigEntity()

@HiltViewModel
class WelcomeMessageViewModel @Inject constructor(
    private val repository: WelcomeMessageRepository,
) : ViewModel() {

    val config: StateFlow<WelcomeMessageConfigEntity> = repository.getConfig()
        .map { it ?: DefaultConfig }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DefaultConfig,
        )

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun updateConfig(transform: WelcomeMessageConfigEntity.() -> WelcomeMessageConfigEntity) {
        viewModelScope.launch {
            repository.upsertConfig(config.value.transform())
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun setEnabled(enabled: Boolean) = updateConfig { copy(isEnabled = enabled) }

    fun setCooldownDays(days: Int) = updateConfig { copy(cooldownDays = days.coerceIn(1, 365)) }

    fun setMessage(message: String) = updateConfig { copy(message = message.trim()) }

    fun resetContactTracking() {
        viewModelScope.launch { repository.clearAllContacts() }
    }
}
