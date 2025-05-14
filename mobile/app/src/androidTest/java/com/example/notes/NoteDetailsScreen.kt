package com.example.notes

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.example.notes.data.network.Priority
import com.example.notes.presentation.ui.NoteDetailsTestTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class NoteDetailsScreenRobot(
    provider: SemanticsNodeInteractionsProvider
) : ComposeScreen<NoteDetailsScreenRobot>(provider) {
    val root: KNode = child { hasTestTag(NoteDetailsTestTags.RootColumn) }
    val backButton: KNode = child { hasTestTag(NoteDetailsTestTags.BackButton) }
    val editButton: KNode = child { hasTestTag(NoteDetailsTestTags.EditButton) }
    val titleField: KNode = child { hasTestTag(NoteDetailsTestTags.TitleField) }
    val deadlineLabel: KNode = child { hasTestTag(NoteDetailsTestTags.DeadlineLabel) }
    val deadlineValue: KNode = child { hasTestTag(NoteDetailsTestTags.DeadlineValue) }
    val deadlinePicker: KNode = child { hasTestTag(NoteDetailsTestTags.DeadlinePicker) }
    val priorityLabel: KNode = child { hasTestTag(NoteDetailsTestTags.PriorityLabel) }
    val priorityValue: KNode = child { hasTestTag(NoteDetailsTestTags.PriorityValue) }
    val priorityMenu: KNode = child { hasTestTag(NoteDetailsTestTags.PriorityMenu) }
    val bodyField: KNode = child { hasTestTag(NoteDetailsTestTags.BodyField) }

    fun dropdownItem(option: Priority): KNode =
        child { hasTestTag(NoteDetailsTestTags.DropdownItemPrefix + option) }
}
