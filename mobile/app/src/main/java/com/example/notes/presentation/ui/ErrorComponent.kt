package com.example.notes.presentation.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource

@Composable
fun ErrorComponent(message: String, onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Ошибка") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onRetry,
                modifier = Modifier.testTag(NoteDetailsTestTags.RetryButton)) {
                Text(text = "Попробовать снова")
            }
        },
        modifier = Modifier.testTag(NoteDetailsTestTags.ErrorDialog)
    )
}