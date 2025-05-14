package com.example.notes

import androidx.activity.ComponentActivity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notes.data.network.Priority
import com.example.notes.data.network.Status
import com.example.notes.data.network.TaskDto
import com.example.notes.data.repository.TaskRepository
import com.example.notes.presentation.MainActivity
import com.example.notes.presentation.ui.NoteDetailsScreen
import com.example.notes.presentation.ui.NoteDetailsTestTags
import com.example.notes.presentation.uistate.NoteContent
import com.example.notes.presentation.viewmodel.EditNoteViewModel
import com.example.notes.presentation.viewmodel.INoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class NoteDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var noteDetailsScreen: NoteDetailsScreenRobot

    private val fakeRepository = object : TaskRepository {
        override suspend fun getAll(sort: String?): List<TaskDto> {
            TODO("Not yet implemented")
        }

        @Throws(HttpException::class)
        override suspend fun getById(id: Long): TaskDto {


                return TaskDto(
                    id = id,
                    title = "Test Title",
                    description = "Test Body",
                    status = Status.Active,
                    deadline = "31.05.2025",
                    priority = Priority.Medium,
                    createdAt = null,
                    updatedAt = null
                )
            }


        override suspend fun create(task: TaskDto): TaskDto {
            TODO("Not yet implemented")
        }

        override suspend fun update(task: TaskDto): TaskDto {
            return when (task.id) {
                404L -> throw HttpException(
                    retrofit2.Response.error<Any>(
                        404,
                        ResponseBody.create(
                            "application/json".toMediaTypeOrNull(),
                            """{"message": "Заметка не найдена"}"""
                        )
                    )
                )

                400L -> throw HttpException(
                    retrofit2.Response.error<Any>(
                        400,
                        ResponseBody.create("application/json".toMediaTypeOrNull(), "Bad Request")
                    )
                )
                else -> TaskDto(
                    id = task.id,
                    title = "Test Title",
                    description = "Test Body",
                    status = Status.Active,
                    deadline = "31.05.2025",
                    priority = Priority.Medium,
                    createdAt = null,
                    updatedAt = null
                )
            }

        }

        override suspend fun delete(id: Long) {
            TODO("Not yet implemented")
        }

        override suspend fun complete(id: Long): TaskDto {
            TODO("Not yet implemented")
        }

        override suspend fun uncomplete(id: Long): TaskDto {
            TODO("Not yet implemented")
        }
    }


    @Before
    fun setup() {
        Dispatchers.resetMain()
        noteDetailsScreen = NoteDetailsScreenRobot(composeTestRule)
    }

    private fun launch(noteId: Long = 1L) {

    }

    @Test
    fun noteDetails_displaysLoadedContent() {
        launch(1L)
        noteDetailsScreen.titleField.assertTextEquals("Test Title")
        noteDetailsScreen.bodyField.assertTextEquals("Test Body")
        noteDetailsScreen.deadlineValue.assertTextEquals("31.05.2025")
        noteDetailsScreen.priorityValue.assertTextEquals("Medium")
    }

    @Test
    fun editButton_togglesEditMode_andSaves() {
        launch(2L)
        noteDetailsScreen.titleField.assertIsNotEnabled()
        noteDetailsScreen.priorityMenu.assertIsNotEnabled()
        noteDetailsScreen.bodyField.assertIsNotEnabled()

        noteDetailsScreen.editButton.performClick()

        noteDetailsScreen.titleField.assertIsEnabled()
        noteDetailsScreen.priorityMenu.assertIsEnabled()
        noteDetailsScreen.bodyField.assertIsEnabled()

        noteDetailsScreen.titleField.performClick()
        composeTestRule.waitForIdle()
        noteDetailsScreen.titleField.performTextClearance()
        noteDetailsScreen.titleField.performTextInput("Updated Title")

        noteDetailsScreen.bodyField.performClick()
        noteDetailsScreen.bodyField.performTextClearance()
        noteDetailsScreen.bodyField.performTextInput("Updated Body")

        noteDetailsScreen.editButton.performClick()
        composeTestRule.waitForIdle()
        noteDetailsScreen.titleField.assertTextEquals("Updated Title")
        noteDetailsScreen.bodyField.assertTextEquals("Updated Body")
    }

    @Test
    fun priorityDropdown_changesPriorityValue() {
        launch(1L)
        noteDetailsScreen.editButton.performClick()
        composeTestRule.waitForIdle()

        noteDetailsScreen.priorityMenu.performClick()
        composeTestRule.waitForIdle()
        noteDetailsScreen.dropdownItem(Priority.High).performClick()
        composeTestRule.waitForIdle()

        noteDetailsScreen.priorityValue.assertTextEquals("High")
    }

    @Test
    fun a_errorComponent_showsAndRetryWorks() {

        launch(400L)
        noteDetailsScreen.editButton.performClick()
        noteDetailsScreen.titleField.performClick()
        composeTestRule.waitForIdle()
        noteDetailsScreen.titleField.performTextClearance()
        noteDetailsScreen.titleField.performTextInput("abc")
        noteDetailsScreen.editButton.performClick()
        composeTestRule.onNodeWithTag(NoteDetailsTestTags.ErrorDialog, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NoteDetailsTestTags.RetryButton, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

class TestNoteViewModel(
    initialNotes: List<NoteContent>
) : INoteViewModel {
    private val _notes = mutableStateOf(initialNotes)
    override val notes: State<List<NoteContent>> get() = _notes

    override fun loadTasks(type: String?) {

    }

    override fun deleteNote(noteId: Long?) {
        _notes.value = _notes.value.filter { it.id != noteId }
    }

    override fun onNoteStatusChanged(noteId: Long?, isChecked: Boolean) {
        val currentDate = LocalDate.now()

        _notes.value = _notes.value.map { note ->
            if (note.id == noteId) {
                val deadlineDate = try {
                    note.deadline?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)) }
                } catch (e: Exception) {
                    null
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
    }

}