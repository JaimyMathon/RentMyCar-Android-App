package com.example.rentmycar_android_app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OSRMClient {
    private const val BASE_URL = "https://router.project-osrm.org/"

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
