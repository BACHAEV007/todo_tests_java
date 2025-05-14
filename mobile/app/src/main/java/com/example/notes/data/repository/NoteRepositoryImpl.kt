package com.example.notes.data.repository

import com.example.notes.data.datasource.NoteDataSource
import com.example.notes.data.entity.Note
import com.example.notes.data.network.RetrofitClient
import com.example.notes.data.network.TaskApiService
import com.example.notes.data.network.TaskDto
import com.example.notes.domain.repository.NoteRepository
import okhttp3.internal.concurrent.Task
import java.time.LocalDateTime
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val api: TaskApiService
) : TaskRepository {

    override suspend fun getAll(sort: String?): List<TaskDto> =
        api.getAll(sort)

    override suspend fun getById(id: Long): TaskDto =
        api.getById(id)

    override suspend fun create(task: TaskDto): TaskDto =
        api.create(task)

    override suspend fun update(task: TaskDto): TaskDto =
        api.update(task.id ?: throw IllegalArgumentException("ID is required for update"), task)

    override suspend fun delete(id: Long) {
        api.delete(id)
    }

    override suspend fun complete(id: Long): TaskDto =
        api.complete(id)

    override suspend fun uncomplete(id: Long): TaskDto =
        api.uncomplete(id)
}

interface TaskRepository {
    suspend fun getAll(sort: String? = null): List<TaskDto>
    suspend fun getById(id: Long): TaskDto
    suspend fun create(task: TaskDto): TaskDto
    suspend fun update(task: TaskDto): TaskDto
    suspend fun delete(id: Long)
    suspend fun complete(id: Long): TaskDto
    suspend fun uncomplete(id: Long): TaskDto
}

class NoteRepositoryImpl @Inject constructor(noteDataSource: NoteDataSource
) : NoteRepository {
    override suspend fun addNote(note: com.example.notes.domain.model.Note) {
        TODO("Not yet implemented")
    }

    override suspend fun updateNote(note: com.example.notes.domain.model.Note) {
        TODO("Not yet implemented")
    }

    override suspend fun loadNotes(): List<com.example.notes.domain.model.Note> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteNote(noteId: String) {
        TODO("Not yet implemented")
    }

}

