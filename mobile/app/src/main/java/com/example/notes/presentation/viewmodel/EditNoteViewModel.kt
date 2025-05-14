package com.example.notes.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.network.Priority
import com.example.notes.data.network.Status
import com.example.notes.data.network.TaskDto
import com.example.notes.data.repository.TaskRepository
import com.example.notes.domain.model.Note
import com.example.notes.presentation.uistate.NoteContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _noteContent = MutableStateFlow<NoteContent?>(null)
    val noteContent: StateFlow<NoteContent?> = _noteContent
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    fun loadTask(id: Long) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getById(id)
                _noteContent.update { currentContent ->
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

    fun editNote(id: Long,title: String, body: String, priority: Priority, deadline: String?) {
        val noteToSave = TaskDto(
            id = id,
            title = title,
            description = body,
            priority = priority,
            deadline = if (deadline == null || deadline == "Без срока") null else convertHumanToIso(deadline),
            status = Status.Active,
            createdAt = null,
            updatedAt = null
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                taskRepository.update(noteToSave)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 400) {
                    val code = e.code()
                    val raw = e.response()?.errorBody()?.string().orEmpty()
                    val message = runCatching {
                        JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
                    }.getOrNull() ?: raw

                    _errorMessage.value = "$code: $message"
                } else {
                    _errorMessage.value = "Ошибка сервера: ${e.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Неожиданная ошибка"
            }

        }
    }

    fun convertHumanToIso(dateString: String): String =
        runCatching {
            val humanFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
            val date = LocalDate.parse(dateString, humanFmt)
            date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrElse { dateString }
    fun clearError() {
        _errorMessage.value = null
    }
}