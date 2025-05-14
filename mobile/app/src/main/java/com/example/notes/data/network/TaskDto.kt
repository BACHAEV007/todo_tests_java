package com.example.notes.data.network

import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime


@JsonClass(generateAdapter = true)
data class TaskDto(
    val id: Long? = null,
    val title: String,
    val description: String?,
    val deadline: String?,
    val status: Status,
    val priority: Priority?,
    val createdAt: String?,
    val updatedAt: String?
)

enum class Status {
    Active,
    Completed,
    Overdue,
    Late
}

enum class Priority {
    Low,
    Medium,
    High,
    Critical
}