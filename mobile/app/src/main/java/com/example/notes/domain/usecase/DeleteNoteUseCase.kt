package com.example.notes.domain.usecase

import com.example.notes.domain.repository.NoteRepository

class DeleteNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(id: String) {
        return noteRepository.deleteNote(noteId = id)
    }
}