package com.example.notes.presentation.uistate

import com.example.notes.data.network.Priority
import com.example.notes.data.network.Status
import java.util.UUID

data class NoteContent(
    val id: Long?,
    val title: String = "",
    val body: String = "",
    val status: Status = Status.Active,
    val deadline: String = "16 декабря 2022",
    val priority: Priority = Priority.Medium
)


