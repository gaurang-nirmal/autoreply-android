package com.psspl.autoreply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.utils.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppSettingsRepository,
) : ViewModel() {

    val themeMode = repository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    // Eagerly so the Room query starts immediately when the ViewModel is created,
    // ensuring .value reflects the persisted setting by the time onResume() fires.
    val appLockEnabled = repository.appLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    /** Direct DB read — safe to call from onResume() via lifecycleScope. */
    suspend fun isAppLockEnabled(): Boolean =
        repository.get()?.appLockEnabled ?: false
}
