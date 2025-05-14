package com.example.notes.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface TaskApiService {

    @GET("api/tasks")
    suspend fun getAll(@Query("sort") sort: String? = null): List<TaskDto>

    @GET("api/tasks/{id}")
    suspend fun getById(@Path("id") id: Long): TaskDto

    @POST("api/tasks")
    suspend fun create(@Body dto: TaskDto): TaskDto

    @PUT("api/tasks/{id}")
    suspend fun update(@Path("id") id: Long, @Body dto: TaskDto): TaskDto

    @DELETE("api/tasks/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>

    @PATCH("api/tasks/{id}/complete")
    suspend fun complete(@Path("id") id: Long): TaskDto

    @PATCH("api/tasks/{id}/uncomplete")
    suspend fun uncomplete(@Path("id") id: Long): TaskDto
}