package com.example.notes.data.datasource

import android.content.Context
import com.example.notes.data.entity.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteDataSource(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(NOTE_PREFERENCES_KEY, Context.MODE_PRIVATE)
    fun loadNotes(): List<Note> {
        val notesJson = sharedPreferences.getString("notes", "[]") ?: "[]"
        return Gson().fromJson(notesJson, object : TypeToken<List<Note>>() {}.type)
    }

    fun saveNote(note: Note) {
        val existingNotes = loadNotes().toMutableList()
        val index = existingNotes.indexOfFirst { it.id == note.id }
        if (index == -1) {
            existingNotes.add(note)
        } else {
            existingNotes[index] = note
        }

        sharedPreferences.edit()
            .putString("notes", Gson().toJson(existingNotes))
            .apply()
    }

    fun updateNote(updatedNote: Note) {
        val existingNotes = loadNotes().toMutableList()
        val index = existingNotes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            existingNotes[index] = updatedNote
        }

        sharedPreferences.edit()
            .putString("notes", Gson().toJson(existingNotes))
            .apply()
    }

    fun deleteNote(noteId: String) {
        val existingNotes = loadNotes().toMutableList()
        val noteToRemove = existingNotes.find { it.id == noteId }

        if (noteToRemove != null) {
            existingNotes.remove(noteToRemove)
            val updatedNotesJson = Gson().toJson(existingNotes)
            sharedPreferences.edit()
                .putString("notes", updatedNotesJson)
                .apply()
        }
    }

    companion object {
        private const val NOTE_PREFERENCES_KEY = "note_preferences_key"
    }
}