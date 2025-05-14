package com.example.notes.domain.model

import java.util.UUID

data class Note(
    val id: Long,
    val title: String,
    val body: String,
)
