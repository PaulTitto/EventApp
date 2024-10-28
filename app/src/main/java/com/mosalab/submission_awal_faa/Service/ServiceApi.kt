package com.mosalab.submission_awal_faa.Service

import com.mosalab.submission_awal_faa.Data.DetailEventResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object ApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://event-api.dicoding.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: EventApiService = retrofit.create(EventApiService::class.java)
}

interface EventApiService {
    @GET("events")
    suspend fun getEvents(@Query("active") active: String): DetailEventResponse
}
