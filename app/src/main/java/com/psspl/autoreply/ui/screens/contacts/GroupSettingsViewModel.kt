package com.psspl.autoreply.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.ContactConfigEntity
import com.psspl.autoreply.database.entity.GroupEntity
import com.psspl.autoreply.repository.ContactsRepository
import com.psspl.autoreply.utils.GroupMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val repository: ContactsRepository,
) : ViewModel() {

    val config: StateFlow<ContactConfigEntity> = repository.observeConfig()
        .map { it ?: ContactConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContactConfigEntity())

    val groups: StateFlow<List<GroupEntity>> = repository.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setGroupMode(mode: GroupMode) {
        viewModelScope.launch { repository.setGroupMode(mode) }
    }

    fun addGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addGroup(name) }
    }

    fun deleteGroup(group: GroupEntity) {
        viewModelScope.launch { repository.deleteGroup(group) }
    }
}
