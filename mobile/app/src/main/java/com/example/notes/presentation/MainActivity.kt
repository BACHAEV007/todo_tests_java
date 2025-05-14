package com.example.notes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.notes.presentation.navigation.SetupNavGraph
import com.example.notes.presentation.theme.NotesTheme
import com.example.notes.presentation.viewmodel.AddNoteViewModel
import com.example.notes.presentation.viewmodel.EditNoteViewModel
import com.example.notes.presentation.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val noteViewModel: NoteViewModel by viewModels()
    private val addNoteViewModel: AddNoteViewModel by viewModels()
    private val editNoteViewModel: EditNoteViewModel by viewModels()
    lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                ) {
                    navController = rememberNavController()
                    SetupNavGraph(navController = navController, noteViewModel = noteViewModel, addNoteViewModel, editNoteViewModel)
                }
            }
        }
    }
}
