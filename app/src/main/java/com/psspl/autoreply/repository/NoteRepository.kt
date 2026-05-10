package com.psspl.autoreply.repository

import com.psspl.autoreply.database.dao.NoteDao
import com.psspl.autoreply.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao,
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = dao.getAllNotes()

    suspend fun getNoteById(id: Int): NoteEntity? = dao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long = dao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)

    suspend fun deleteNotesByIds(ids: List<Int>) = dao.deleteNotesByIds(ids)
}
