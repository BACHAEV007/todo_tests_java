package com.example.notes.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.network.Priority
import com.example.notes.data.network.Status
import com.example.notes.data.repository.TaskRepository
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.AddNoteUseCase
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.EditNoteUseCase
import com.example.notes.domain.usecase.LoadNotesUseCase
import com.example.notes.presentation.uistate.NoteContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase,
    private val editNoteUseCase: EditNoteUseCase,
    private val loadNotesUseCase: LoadNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val taskRepository: TaskRepository
) : ViewModel(), INoteViewModel {
    override val notes: State<List<NoteContent>>
        get() = _notes
    private val _notes: MutableState<List<NoteContent>> = mutableStateOf(emptyList())

    init {
        loadTasks(null)
    }

    override fun loadTasks(type: String?) {
        viewModelScope.launch {
            try {

                val tasks = if (type == null) taskRepository.getAll() else taskRepository.getAll(type)
                _notes.value = tasks.map { task ->
                    NoteContent(
                        id = task.id,
                        title = task.title,
                        body = task.description ?: "",
                        status = Status.valueOf(task.status.name),
                        deadline = task.deadline?.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                            ?: "Без срока",
                        priority = Priority.valueOf(task.priority!!.name)
                    )
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка при загрузке задач", e)
            }
        }
    }

    override fun deleteNote(noteId: Long?) {
            _notes.value = _notes.value.filter { note -> note.id != noteId }
            viewModelScope.launch(Dispatchers.IO) {
                taskRepository.delete(noteId!!)
            }
        }

    override fun onNoteStatusChanged(noteId: Long?, isChecked: Boolean) {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val updatedNotes = _notes.value.map { note ->
                if (note.id == noteId) {
                    val deadlineDate = note.deadline?.let {
                        try {
                            LocalDate.parse(it, formatter)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val newStatus = when {
                        isChecked && deadlineDate != null -> {
                            if (currentDate.isAfter(deadlineDate)) Status.Late else Status.Completed
                        }
                        !isChecked && deadlineDate != null -> {
                            if (currentDate.isAfter(deadlineDate)) Status.Overdue else Status.Active
                        }
                        isChecked -> Status.Completed
                        else -> Status.Active
                    }

                    note.copy(status = newStatus)
                } else note
            }

            _notes.value = updatedNotes
            viewModelScope.launch {
                if (isChecked) {
                    taskRepository.complete(noteId!!)
                } else {
                    taskRepository.uncomplete(noteId!!)
                }
            }
        }

}

interface INoteViewModel {
    val notes: State<List<NoteContent>>
    fun loadTasks(type: String? = null)
    fun deleteNote(noteId: Long?)
    fun onNoteStatusChanged(noteId: Long?, isChecked: Boolean)
}

