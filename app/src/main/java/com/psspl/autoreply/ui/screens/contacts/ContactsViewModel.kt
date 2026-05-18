package com.psspl.autoreply.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.ContactConfigEntity
import com.psspl.autoreply.database.entity.ContactEntity
import com.psspl.autoreply.repository.ContactsRepository
import com.psspl.autoreply.utils.ContactMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repository: ContactsRepository,
) : ViewModel() {

    val config: StateFlow<ContactConfigEntity> = repository.observeConfig()
        .map { it ?: ContactConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContactConfigEntity())

    val contacts: StateFlow<List<ContactEntity>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setContactMode(mode: ContactMode) {
        viewModelScope.launch { repository.setContactMode(mode) }
    }

    fun setGroupsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setGroupsEnabled(enabled) }
    }

    fun addContact(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addContact(name) }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch { repository.deleteContact(contact) }
    }
}
