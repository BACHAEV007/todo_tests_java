package com.example.notes.domain.repository

import com.example.notes.domain.model.Note

interface NoteRepository {

    suspend fun addNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun loadNotes(): List<Note>

    suspend fun deleteNote(noteId: String)
}