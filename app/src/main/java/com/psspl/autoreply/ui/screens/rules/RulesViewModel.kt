package com.psspl.autoreply.ui.screens.rules

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.backup.KeywordRuleBackupManager
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.KeywordRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val keywordRuleRepository: KeywordRuleRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val backupManager: KeywordRuleBackupManager,
) : ViewModel() {

    // ─── Search ───────────────────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ─── Rules (filtered by search query) ────────────────────────────────────

    val rules: StateFlow<List<KeywordRuleEntity>> = combine(
        keywordRuleRepository.allRules,
        _searchQuery,
    ) { allRules, query ->
        if (query.isBlank()) allRules
        else allRules.filter { rule ->
            rule.keyword.contains(query, ignoreCase = true) ||
                    rule.replyText.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    // ─── Module toggle ────────────────────────────────────────────────────────

    val isModuleEnabled: StateFlow<Boolean> = appSettingsRepository.isAutoReplyEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    // ─── Snackbar feedback ────────────────────────────────────────────────────

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // ─── Public actions ───────────────────────────────────────────────────────

    fun setModuleEnabled(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setAutoReplyEnabled(enabled) }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteRule(rule: KeywordRuleEntity) {
        viewModelScope.launch { keywordRuleRepository.delete(rule) }
    }

    fun clearAllRules() {
        viewModelScope.launch {
            keywordRuleRepository.deleteAll()
            _snackbarMessage.value = "All rules cleared"
        }
    }

    fun exportRules(uri: Uri) {
        viewModelScope.launch {
            val success = backupManager.exportRules(uri)
            _snackbarMessage.value = if (success) "Backup saved successfully" else "Backup failed"
        }
    }

    fun importRules(uri: Uri) {
        viewModelScope.launch {
            val count = backupManager.importRules(uri)
            _snackbarMessage.value = when {
                count > 0 -> "$count rule(s) imported"
                count == 0 -> "No new rules to import (all duplicates)"
                else -> "Restore failed — invalid file"
            }
        }
    }

    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }
}
