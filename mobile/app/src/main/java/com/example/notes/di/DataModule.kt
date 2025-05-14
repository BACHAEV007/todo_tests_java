package com.example.notes.di

import android.content.Context
import com.example.notes.data.datasource.NoteDataSource
import com.example.notes.data.network.TaskApiService
import com.example.notes.data.repository.TaskRepository
import com.example.notes.data.repository.TaskRepositoryImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideNoteDataSource(@ApplicationContext context: Context): NoteDataSource {
        return NoteDataSource(context)
    }
    @Provides
    @Singleton
    fun provideTaskRepository(
        taskApiService: TaskApiService
    ): TaskRepository {
        return TaskRepositoryImpl(api = taskApiService)
    }
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskApiService(retrofit: Retrofit): TaskApiService {
        return retrofit.create(TaskApiService::class.java)
    }
}