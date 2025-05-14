package com.example.notes

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notes.data.network.Priority
import com.example.notes.data.network.Status
import com.example.notes.presentation.MainActivity
import com.example.notes.presentation.ui.MyNotesScreen
import com.example.notes.presentation.ui.MyNotesTestTags
import com.example.notes.presentation.ui.NoteDetailsTestTags
import com.example.notes.presentation.uistate.NoteContent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MyNoteScreenTest {
    @RunWith(AndroidJUnit4::class)
    class WithEmptyComponentActivity {

        @get:Rule
        val composeTestRule = createAndroidComposeRule<ComponentActivity>()
        private var myNotesScreen = MyNotesScreenRobot(composeTestRule)
        private var noteDetailsScreen = NoteDetailsScreenRobot(composeTestRule)

        private fun launch(
            notes: List<NoteContent> = emptyList()
        ){
            val fakeVm = TestNoteViewModel(notes)
            composeTestRule.setContent {
                MyNotesScreen(
                    goToAddNoteScreen = { },
                    goToNoteDetailsScreen = { },
                    modifier = Modifier,
                    noteViewModel = fakeVm
                )
            }
        }

        @Test
        fun deleteNote_updatesList() = runTest {
            val notes = listOf(
                NoteContent(1L, "ABCD", "", Status.Active, "01 Jan", Priority.Medium),
                NoteContent(2L, "BBBBB", "", Status.Active, "01 Jan", Priority.Medium)
            )
            launch(notes)
            composeTestRule.onNodeWithTag("NoteDeleteButton_1", useUnmergedTree = true)
                .performScrollTo()
                .assertExists()
                .assertIsDisplayed()
                .performClick()
            myNotesScreen.noteItem(1L).assertDoesNotExist()
            myNotesScreen.noteTitle(2L).assertTextEquals("BBBBB")
            myNotesScreen.noteDeadline(2L).assertTextEquals("01 Jan")
            composeTestRule.onNodeWithTag(MyNotesTestTags.NotePriorityPrefix + "2")
                .assertExists()

            myNotesScreen.noteStatus(2L).assertTextEquals("Active")
            myNotesScreen.noteItem(2L).assertIsDisplayed()
        }



        @Test
        fun emptyList_showsEmptyState() = runTest {
            launch(emptyList())

            myNotesScreen.emptyImage.assertExists()
            myNotesScreen.emptyText.assertTextContains("No notes. Let’s fix that!")
            myNotesScreen.notesList.assertDoesNotExist()
        }

        @Test
        fun myNotes_checkboxToggle_changesStatus() = runTest {
            val notes = listOf(
                NoteContent(id = 1, title = "ABCD", body = "", status = Status.Active, deadline = "01 Jan", priority = Priority.Low),
                NoteContent(id = 2, title = "BBBBB", body = "", status = Status.Overdue, deadline = "10 May 2005", priority = Priority.Medium)
            )

            launch(notes)
            composeTestRule.waitForIdle()

            myNotesScreen.noteItem(1).assertIsDisplayed()
            myNotesScreen.noteStatus(1).assertTextEquals("Active")
            myNotesScreen.noteCheckbox(1).assertIsOff()

            myNotesScreen.noteCheckbox(1).performClick()
            composeTestRule.waitForIdle()

            myNotesScreen.noteStatus(1).assertTextEquals("Completed")
            myNotesScreen.noteCheckbox(1).assertIsOn()

            myNotesScreen.noteItem(2).assertIsDisplayed()
            myNotesScreen.noteStatus(2).assertTextEquals("Overdue")
            myNotesScreen.noteCheckbox(2).assertIsOff()

            myNotesScreen.noteCheckbox(2).performClick()
            composeTestRule.waitForIdle()

            myNotesScreen.noteStatus(2).assertTextEquals("Late")
            myNotesScreen.noteCheckbox(2).assertIsOn()
        }

        @Test
        fun myNotes_commonUiElements_areDisplayed() = runTest {
            composeTestRule.waitForIdle()
            launch(emptyList())
            composeTestRule.waitForIdle()
            myNotesScreen.rootBox.assertIsDisplayed()
            myNotesScreen.headerRow.assertIsDisplayed()
            myNotesScreen.titleText.assertTextEquals("Notes")
            myNotesScreen.sortButton.assertIsDisplayed()
            myNotesScreen.fabAdd.assertIsDisplayed()

            myNotesScreen.emptyImage.assertIsDisplayed()
            myNotesScreen.emptyText.assertIsDisplayed()
        }
    }

    @RunWith(AndroidJUnit4::class)
    class WithMainActivity {

        @get:Rule
        val composeTestRule = createAndroidComposeRule<MainActivity>()
        private var myNotesScreen = MyNotesScreenRobot(composeTestRule)
        private var noteDetailsScreen = NoteDetailsScreenRobot(composeTestRule)
        @Test
        fun noteDetails_displaysLoadedContent() {
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "23", useUnmergedTree = true)[0].performClick()
            composeTestRule.waitForIdle()
            noteDetailsScreen.titleField.assertTextEquals("1234aBC")
            noteDetailsScreen.bodyField.assertTextEquals("RSDGFHDFGJ")
            noteDetailsScreen.deadlineValue.assertTextEquals("2025-05-06")
            noteDetailsScreen.priorityValue.assertTextEquals("Critical")
        }

        @Test
        fun noteDetails_correct_edit_title() {
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "23", useUnmergedTree = true)[0].performClick()
            composeTestRule.waitForIdle()
            noteDetailsScreen.titleField.assertTextEquals("1234aBC")
            noteDetailsScreen.bodyField.assertTextEquals("RSDGFHDFGJ")
            noteDetailsScreen.deadlineValue.assertTextEquals("2025-05-06")
            noteDetailsScreen.priorityValue.assertTextEquals("Critical")
        }

        @Test
        fun editButton_togglesEditMode_andSaves() {
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "24", useUnmergedTree = true)[0].performClick()
            composeTestRule.waitForIdle()
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
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "24", useUnmergedTree = true)[0].performClick()
            composeTestRule.waitForIdle()
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
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "24", useUnmergedTree = true)[0].performClick()
            noteDetailsScreen.editButton.performClick()
            noteDetailsScreen.titleField.performClick()
            composeTestRule.waitForIdle()
            noteDetailsScreen.titleField.performTextClearance()
            noteDetailsScreen.titleField.performTextInput("abc")
            noteDetailsScreen.editButton.performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(NoteDetailsTestTags.ErrorDialog, useUnmergedTree = true).assertIsDisplayed()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(NoteDetailsTestTags.RetryButton, useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()
        }

        @Test
        fun a_errorComponent_onlyMacros_InTitle_showsAndRetryWorks() {
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "24", useUnmergedTree = true)[0].performClick()
            noteDetailsScreen.editButton.performClick()
            noteDetailsScreen.titleField.performClick()
            composeTestRule.waitForIdle()
            noteDetailsScreen.titleField.performTextClearance()
            noteDetailsScreen.titleField.performTextInput("!1")
            noteDetailsScreen.editButton.performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(NoteDetailsTestTags.ErrorDialog, useUnmergedTree = true).assertIsDisplayed()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(NoteDetailsTestTags.RetryButton, useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()
        }

        @Test
        fun a_errorComponent_onlyMacrosWithData_InTitle_showsAndRetryWorks() {
            val allItems = composeTestRule
                .onAllNodesWithTag(MyNotesTestTags.NoteItemPrefix + "24", useUnmergedTree = true)[0].performClick()
            noteDetailsScreen.editButton.performClick()
            noteDetailsScreen.titleField.performClick()
            composeTestRule.waitForIdle()
            noteDetailsScreen.titleField.performTextClearance()
            noteDetailsScreen.titleField.performTextInput("!1 !before 13.05.2005")
            noteDetailsScreen.editButton.performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(NoteDetailsTestTags.ErrorDialog, useUnmergedTree = true).assertIsDisplayed()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(NoteDetailsTestTags.RetryButton, useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()
        }

        @Test
        fun fabIsDisplayedAndClickable() = runTest {
            myNotesScreen.fabAdd.assertExists()
            myNotesScreen.fabAdd.assertIsDisplayed()
            myNotesScreen.fabAdd.assertHasClickAction()
        }

        @Test
        fun sortDropdown_showsOptions_deadline() = runTest {

            myNotesScreen.sortButton.assertExists()
            myNotesScreen.sortButton.performClick()

            myNotesScreen.dropdownMenu.assertExists()

            myNotesScreen.dropdownItem("deadline").assertExists()
            myNotesScreen.dropdownItem("created").assertExists()
            myNotesScreen.dropdownItem("deadline").performClick()
            composeTestRule.waitForIdle()

        }

        @Test
        fun sortDropdown_showsOptions_created() = runTest {

            myNotesScreen.sortButton.assertExists()
            myNotesScreen.sortButton.performClick()

            myNotesScreen.dropdownMenu.assertExists()

            myNotesScreen.dropdownItem("deadline").assertExists()
            myNotesScreen.dropdownItem("created").assertExists()
            myNotesScreen.dropdownItem("created").performClick()
            composeTestRule.waitForIdle()

        }

    }


}