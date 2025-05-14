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
import com.example.notes.presentation.uistate.NoteContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    val notes: State<NoteContent>
        get() = _notes
    private val _notes: MutableState<NoteContent> = mutableStateOf(NoteContent(id = null))
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addNote(title: String, body: String, deadline: String?, priority: Priority?) {
        val noteToSave = TaskDto(
            title = title,
            description = body,
            priority = if (priority != null) priority else null,
            deadline = if (deadline != null) convertRussianDateToIso(deadline) else null,
            status = Status.Active,
            createdAt = null,
            updatedAt = null
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                taskRepository.create(noteToSave)
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

    fun convertRussianDateToIso(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        val date = LocalDate.parse(dateString, inputFormatter)
        return date.format(outputFormatter)
    }

    fun clearError() {
        _errorMessage.value = null
    }

}