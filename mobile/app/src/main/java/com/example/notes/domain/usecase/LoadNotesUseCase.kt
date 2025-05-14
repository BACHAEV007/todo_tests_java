package com.example.notes.domain.usecase

import com.example.notes.domain.model.Note
import com.example.notes.domain.repository.NoteRepository

class LoadNotesUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(): List<Note> {
        return noteRepository.loadNotes()
    }
}