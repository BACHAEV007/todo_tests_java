package com.example.notes
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.example.notes.presentation.ui.MyNotesTestTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class MyNotesScreenRobot(provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MyNotesScreenRobot>(provider) {

    val rootBox: KNode = child { hasTestTag(MyNotesTestTags.RootBox) }
    val headerRow: KNode = child { hasTestTag(MyNotesTestTags.HeaderRow) }
    val titleText: KNode = child { hasTestTag(MyNotesTestTags.TitleText) }
    val sortButton: KNode = child { hasTestTag(MyNotesTestTags.SortButton) }
    val dropdownMenu: KNode = child { hasTestTag(MyNotesTestTags.DropdownMenu) }
    fun dropdownItem(option: String): KNode = child { hasTestTag(MyNotesTestTags.DropdownItemPrefix + option) }

    val emptyState: KNode = child { hasTestTag(MyNotesTestTags.EmptyStateColumn) }
    val emptyImage: KNode = child { hasTestTag(MyNotesTestTags.EmptyImage) }
    val emptyText: KNode = child { hasTestTag(MyNotesTestTags.EmptyText) }

    val notesList: KNode = child { hasTestTag(MyNotesTestTags.NotesList) }
    fun noteItem(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NoteItemPrefix + id) }
    fun noteTitle(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NoteTitlePrefix + id) }
    fun noteDeadline(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NoteDeadlinePrefix + id) }
    fun noteStatus(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NoteStatusPrefix + id) }
    fun notePriority(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NotePriorityPrefix + id) }
    fun noteCheckbox(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NoteCheckboxPrefix + id) }
    fun noteDeleteButton(id: Long): KNode = child { hasTestTag(MyNotesTestTags.NoteDeleteButtonPrefix + id) }

    val fabAdd: KNode = child { hasTestTag(MyNotesTestTags.FabAdd) }
}