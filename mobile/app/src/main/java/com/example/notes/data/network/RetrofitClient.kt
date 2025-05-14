package com.example.notes.data.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.Moshi.*
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RetrofitClient {
    private val moshi = Builder()
        .add(LocalDateAdapter())
        .add(LocalDateTimeAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val taskApi: TaskApiService = retrofit.create(TaskApiService::class.java)
}


class LocalDateAdapter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @ToJson
    fun toJson(value: LocalDate?): String? = value?.format(formatter)

    @FromJson
    fun fromJson(value: String?): LocalDate? = LocalDate.parse(value, formatter)
}

class LocalDateTimeAdapter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @ToJson
    fun toJson(value: LocalDateTime): String = value.format(formatter)

    @FromJson
    fun fromJson(value: String): LocalDateTime = LocalDateTime.parse(value, formatter)
}