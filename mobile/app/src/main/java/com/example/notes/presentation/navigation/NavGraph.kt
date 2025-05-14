package com.example.notes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notes.presentation.screen.Screen
import com.example.notes.presentation.ui.AddNoteScreen
import com.example.notes.presentation.ui.MyNotesScreen
import com.example.notes.presentation.ui.NoteDetailsScreen
import com.example.notes.presentation.viewmodel.AddNoteViewModel
import com.example.notes.presentation.viewmodel.EditNoteViewModel
import com.example.notes.presentation.viewmodel.NoteViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    noteViewModel: NoteViewModel,
    addNoteViewModel: AddNoteViewModel,
    editNoteViewModel: EditNoteViewModel
){
    NavHost(
        navController = navController,
        startDestination = Screen.MyNotes.route
    ){
        composable(
            route = Screen.MyNotes.route
        ){
            MyNotesScreen(
                goToAddNoteScreen = {
                    navController.navigate(Screen.AddNote.route)
                },
                goToNoteDetailsScreen = { id ->
                    navController.navigate(Screen.NoteDetail.passTitleAndBody(id))
                },
                noteViewModel = noteViewModel
            )
        }
        composable(
            route = Screen.AddNote.route
        ){
            AddNoteScreen(
                navController = navController,
                goToMyNotesScreen = {
                    navController.navigate(Screen.MyNotes.route){
                        popUpTo(Screen.MyNotes.route) {
                            inclusive = true
                        }
                    }

                },
                noteViewModel = addNoteViewModel
            )
        }
        composable(
            route = "note_details_screen/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")


            NoteDetailsScreen(
                goToMyNotesScreen = {
                    navController.navigate(Screen.MyNotes.route) {
                        popUpTo(Screen.MyNotes.route) {
                            inclusive = true
                        }
                    }
                },
                noteViewModel = editNoteViewModel,
                noteId = id!!
            )

        }
    }
}


