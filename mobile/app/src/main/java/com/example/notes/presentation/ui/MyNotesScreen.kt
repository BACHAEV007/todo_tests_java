package com.example.notes.presentation.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.notes.R
import com.example.notes.data.network.Priority
import com.example.notes.data.network.Status
import com.example.notes.presentation.viewmodel.INoteViewModel
import com.example.notes.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyNotesScreen(
    goToAddNoteScreen: () -> Unit,
    goToNoteDetailsScreen: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    noteViewModel: INoteViewModel,
) {
    var isHolding by remember { mutableStateOf(false) }
    val notesContent by noteViewModel.notes
    var expanded by remember { mutableStateOf(false) }
    val typeOptions = listOf("deadline", "created")
    val formatter = remember {
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    }

    LaunchedEffect(Unit) {
        noteViewModel.loadTasks()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(MyNotesTestTags.RootBox)
            .background(Color(0xFF1F1F1F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 60.dp)
        ) {
            Row (modifier = Modifier.testTag(MyNotesTestTags.HeaderRow)){
                Text(
                    text = "Notes",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(MyNotesTestTags.TitleText)
                )
                IconButton(
                    onClick = {expanded = true},
                    modifier = Modifier
                        .size(40.dp)
                        .testTag(MyNotesTestTags.SortButton)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.file_ic),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = modifier
                        .fillMaxWidth()
                        .testTag(MyNotesTestTags.DropdownMenu),
                ) {
                    typeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.testTag(MyNotesTestTags.DropdownItemPrefix + option),
                            onClick = {
                                noteViewModel.loadTasks(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier.padding(16.dp)
            )
            if (notesContent.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.non_notes_image),
                        contentDescription = "Нет заметок",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.testTag(MyNotesTestTags.EmptyImage)
                    )
                    Text(
                        text = "No notes. Let’s fix that!",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 22.sp,
                        modifier = Modifier.testTag(MyNotesTestTags.EmptyText)
                    )
                }
            } else {
                LazyColumn (modifier = Modifier.testTag(MyNotesTestTags.NotesList)){
                    items(notesContent) { note ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3B3B3B), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .testTag(MyNotesTestTags.NoteItemPrefix + note.id)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            isHolding = true
                                        },
                                        onTap = {
                                            goToNoteDetailsScreen(note.id)
                                        }
                                    )
                                }
                        ) {
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .offset(y = 2.dp)
                                            .background(
                                                color = determineColor(
                                                    note.deadline,
                                                    formatter
                                                ), shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = note.deadline,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag(MyNotesTestTags.NoteDeadlinePrefix + note.id)
                                    )
                                    Text(
                                        text = note.status.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .offset(y = (-4).dp)
                                            .background(
                                                color = when (note.status) {
                                                    Status.Completed -> Color.Green
                                                    Status.Active -> Color.Yellow
                                                    Status.Overdue -> Color.Red
                                                    Status.Late -> Color.Red
                                                }, shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(4.dp)
                                            .testTag(MyNotesTestTags.NoteStatusPrefix + note.id)
                                    )
                                    Spacer(modifier = Modifier.size(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .offset(y = (-16).dp)
                                            .background(
                                                color = when (note.priority) {
                                                    Priority.Low -> Color.White
                                                    Priority.Medium -> Color.Cyan
                                                    Priority.High -> Color.Yellow
                                                    Priority.Critical -> Color.Red
                                                },
                                                shape = RoundedCornerShape(
                                                    bottomStart = 8.dp,
                                                    bottomEnd = 8.dp
                                                )
                                            ).testTag(MyNotesTestTags.NotePriorityPrefix + note.id)
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Checkbox(
                                        checked = note.status == Status.Completed || note.status == Status.Late,
                                        onCheckedChange = { isChecked ->
                                            noteViewModel.onNoteStatusChanged(note.id, isChecked)
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.CenterVertically)
                                            .testTag(MyNotesTestTags.NoteCheckboxPrefix + note.id)
                                    )
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 28.sp,
                                        color = Color.White,
                                        modifier = modifier
                                            .padding(8.dp)
                                            .weight(1f)
                                            .padding(start = 8.dp)
                                            .testTag(MyNotesTestTags.NoteTitlePrefix + note.id)
                                    )
                                    Spacer(
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Column(modifier = Modifier.fillMaxHeight()) {
                                        IconButton(
                                            onClick = { noteViewModel.deleteNote(note.id) },
                                            colors = IconButtonDefaults.iconButtonColors()
                                                .copy(
                                                    containerColor = Color(0xFF1F1F1F),
                                                    disabledContainerColor = Color(0xFF1F1F1F)
                                                ),
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag(MyNotesTestTags.NoteDeleteButtonPrefix + note.id)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.delete_icon),
                                                contentDescription = "Удалить заметку",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                    }
                                }
                            }

                        }

                        Spacer(
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { goToAddNoteScreen() },
            containerColor = Color(0xFF3B3B3B),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 50.dp, vertical = 70.dp)
                .testTag(MyNotesTestTags.FabAdd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Добавить заметку",
                tint = Color.White
            )
        }
    }
}
private fun parseDeadline(deadlineStr: String, formatter: DateTimeFormatter): LocalDate? {
    return try {
        LocalDate.parse(deadlineStr)
    } catch (_: DateTimeParseException) {
        try {
            LocalDate.parse(deadlineStr, formatter)
        } catch (_: Exception) {
            null
        }
    }
}

private fun determineColor(deadlineStr: String, formatter: DateTimeFormatter): Color {
    if (deadlineStr == "Без срока") return Color.Gray

    val date = parseDeadline(deadlineStr, formatter)
        ?: return Color.Gray

    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), date)
    return when {
        daysUntil < 0 -> Color.Red
        daysUntil in 0..2 -> Color(0xFFFFA500)
        else -> Color.Gray
    }
}
