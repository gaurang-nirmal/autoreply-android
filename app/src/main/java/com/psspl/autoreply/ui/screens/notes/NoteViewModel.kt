package com.psspl.autoreply.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.autoreply.database.entity.NoteEntity
import com.psspl.autoreply.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    // ─── Notes list ───────────────────────────────────────────────────────────

    val notes: StateFlow<List<NoteEntity>> = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ─── Multi-selection ──────────────────────────────────────────────────────

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun enterSelectionMode(noteId: Int) {
        _selectedIds.value = setOf(noteId)
    }

    fun toggleSelection(noteId: Int) {
        _selectedIds.update { current ->
            if (noteId in current) current - noteId else current + noteId
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Inserts or updates a note and returns the persisted ID.
     * For a new note ([noteId] == 0) Room auto-generates the ID and it is returned.
     * For an existing note the same [noteId] is returned after the update.
     * Returns [noteId] unchanged when [content] is blank.
     */
    suspend fun saveNoteReturningId(noteId: Int, content: String): Int {
        if (content.isBlank()) return noteId
        return if (noteId == 0) {
            repository.insertNote(NoteEntity(content = content)).toInt()
        } else {
            repository.getNoteById(noteId)?.let { existing ->
                repository.updateNote(
                    existing.copy(
                        content = content,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }
            noteId
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    /** Deletes all currently selected notes and exits selection mode. */
    fun deleteSelected() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            repository.deleteNotesByIds(ids)
            clearSelection()
        }
    }
}
