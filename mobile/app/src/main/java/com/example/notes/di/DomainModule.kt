package com.example.notes.di

import com.example.notes.data.datasource.NoteDataSource
import com.example.notes.data.network.TaskApiService
import com.example.notes.data.repository.NoteRepositoryImpl
import com.example.notes.data.repository.TaskRepository
import com.example.notes.data.repository.TaskRepositoryImpl
import com.example.notes.domain.repository.NoteRepository
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.LoadNotesUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideNoteRepository(noteDataSource: NoteDataSource): NoteRepository {
        return NoteRepositoryImpl(noteDataSource)
    }

//    @Binds
//    @Singleton
//    fun bindTaskRepository(
//        impl: TaskRepositoryImpl
//    ): TaskRepository = TaskRepositoryImpl(api = TaskApiService)

    @Provides
    fun provideLoadNotesUseCase(noteRepository: NoteRepository): LoadNotesUseCase {
        return LoadNotesUseCase(noteRepository)
    }

    @Provides
    fun provideDeleteNoteUseCase(
        noteRepository: NoteRepository
    ): DeleteNoteUseCase {
        return DeleteNoteUseCase(noteRepository)
    }
}