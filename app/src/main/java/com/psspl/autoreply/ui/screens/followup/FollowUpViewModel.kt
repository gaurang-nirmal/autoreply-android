package com.psspl.autoreply.ui.screens.followup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.FollowUpContactEntity
import com.psspl.autoreply.database.entity.FollowUpHistoryEntity
import com.psspl.autoreply.repository.FollowUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowUpViewModel @Inject constructor(
    private val repo: FollowUpRepository,
) : ViewModel() {

    val config = repo.config
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val history = repo.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val specificContacts = repo.contacts("SPECIFIC")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val excludeContacts = repo.contacts("EXCLUDE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleEnabled() = viewModelScope.launch { repo.toggleEnabled() }

    fun setMessage(message: String) = viewModelScope.launch { repo.setMessage(message) }

    fun setScope(scope: String) = viewModelScope.launch { repo.setScope(scope) }

    fun deleteHistory(entry: FollowUpHistoryEntity) =
        viewModelScope.launch { repo.deleteHistory(entry) }

    fun clearHistory() = viewModelScope.launch { repo.clearHistory() }

    fun addContact(listType: String, text: String) = viewModelScope.launch {
        repo.insertContact(FollowUpContactEntity(listType = listType, messageText = text))
    }

    fun deleteContact(contact: FollowUpContactEntity) =
        viewModelScope.launch { repo.deleteContact(contact) }

    fun clearContacts(listType: String) =
        viewModelScope.launch { repo.clearContacts(listType) }
}
