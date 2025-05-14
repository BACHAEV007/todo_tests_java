package com.example.notes.presentation.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notes.R
import com.example.notes.data.network.Priority
import com.example.notes.presentation.uistate.NoteContent
import com.example.notes.presentation.viewmodel.EditNoteViewModel
import com.example.notes.presentation.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun NoteDetailsScreen (
    goToMyNotesScreen: () -> Unit,
    noteViewModel: EditNoteViewModel,
    noteId: Long,
) {
    val context = LocalContext.current
    val noteContent by noteViewModel.noteContent.collectAsState()
    LaunchedEffect (Unit){
        noteViewModel.loadTask(noteId)
    }
    val error by noteViewModel.errorMessage.collectAsState()
    error?.let { msg ->
        ErrorComponent(
            message = msg,
            onRetry = { noteViewModel.clearError() }
        )
    }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    var deadline by remember { mutableStateOf(noteContent?.deadline ?: "Без срока")}
    val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

    var expanded by remember { mutableStateOf(false) }
    val typeOptions = listOf(Priority.Low, Priority.Medium, Priority.High, Priority.Critical)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val formattedDate = displayFormat.format(calendar.time)
                deadline = formattedDate
            },
            year,
            month,
            day
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    var noteTitle by remember { mutableStateOf(noteContent?.title ?: "") }
    var noteBody by remember { mutableStateOf(noteContent?.body ?: "") }
    var priority by remember {
        mutableStateOf(noteContent?.priority ?: Priority.Medium)
    }
    var isFocusedTitle by remember { mutableStateOf(false) }
    var isFocusedBody by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val titleHint = "Title..."
    val bodyHint = "Type something..."
    LaunchedEffect(noteContent) {
        noteContent?.let { nc ->
            noteTitle = nc.title
            noteBody  = nc.body
            deadline  = nc.deadline
            priority  = nc.priority
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 60.dp)
            .testTag(NoteDetailsTestTags.RootColumn)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            IconButton(
                onClick = { goToMyNotesScreen() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF3B3B3B))
                    .testTag(NoteDetailsTestTags.BackButton)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_left),
                    contentDescription = "Добавить заметку",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                    if (isEditing) {
                        noteViewModel.editNote(
                            id = noteContent!!.id!!,
                            title = noteTitle,
                            body = noteBody,
                            priority = priority,
                            deadline = deadline
                        )
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF3B3B3B))
                    .testTag(NoteDetailsTestTags.EditButton)
            ) {
                Icon(
                    painter = painterResource(id = if (isEditing) R.drawable.accept_icon else R.drawable.edit_icon),
                    contentDescription = "Редактировать заметку",
                    tint = (if (isEditing) Color.Green else Color.White),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(
            modifier = Modifier.size(32.dp)
        )
        BasicTextField(
            value = if (noteTitle.isEmpty() && !isFocusedTitle) titleHint else noteTitle,
            onValueChange = { newValue -> noteTitle = newValue },
            textStyle = TextStyle(
                color = if (noteTitle.isEmpty() && !isFocusedTitle) Color.Gray else Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold
            ),
            enabled = isEditing,
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .onFocusChanged { focusState ->
                    isFocusedTitle = focusState.isFocused
                    if (noteTitle.isEmpty() && !isFocusedTitle) noteTitle = ""
                }
                .testTag(NoteDetailsTestTags.TitleField)
        )
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Gray)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Row (modifier = Modifier.fillMaxWidth()){
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Deadline",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                        .testTag(NoteDetailsTestTags.DeadlineLabel)
                )
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = if (deadline.isEmpty()) "Deadline" else deadline,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .testTag(NoteDetailsTestTags.DeadlineValue),
                        color = if (deadline.isEmpty()) Color.Gray else Color.White
                    )
                    IconButton(
                        onClick = {datePickerDialog.show()},
                        enabled = isEditing,
                        modifier = Modifier.testTag(NoteDetailsTestTags.DeadlinePicker)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.calendar_ic),
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column (modifier = Modifier.weight(1f)){
                Text(
                    text = "Priority", color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                        .testTag(NoteDetailsTestTags.PriorityLabel)
                )
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = priority.toString(), modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .testTag(NoteDetailsTestTags.PriorityValue),
                        color = Color.White
                    )
                    IconButton(
                        onClick = { expanded = true },
                        enabled = isEditing,
                        modifier = Modifier.testTag(NoteDetailsTestTags.PriorityMenu)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.file_ic),
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }

        }
        Spacer(modifier = Modifier.size(16.dp))
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Gray)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 24.dp)

        ) {
            typeOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString(), style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        priority = option
                        expanded = false
                    },
                    modifier = Modifier.testTag(NoteDetailsTestTags.DropdownItemPrefix + option)
                )
            }
        }
        BasicTextField(
            value = if (noteBody.isEmpty() && !isFocusedBody) bodyHint else noteBody,
            onValueChange = { newValue -> noteBody = newValue },
            textStyle = TextStyle(
                color = if (noteBody.isEmpty() && !isFocusedBody) Color.Gray else Color.White,
                fontSize = 16.sp,
            ),
            enabled = isEditing,
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .onFocusChanged { focusState ->
                    isFocusedBody = focusState.isFocused
                    if (noteBody.isEmpty() && !isFocusedBody) noteBody = ""
                }
                .testTag(NoteDetailsTestTags.BodyField)
        )
    }
}
