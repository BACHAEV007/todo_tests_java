package com.example.notes.di

import com.example.notes.data.repository.TaskRepository
import com.example.notes.data.repository.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//abstract class RepositoryModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindTaskRepository(
//        impl: TaskRepositoryImpl
//    ): TaskRepository
//}