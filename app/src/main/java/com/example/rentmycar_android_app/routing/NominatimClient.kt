package com.example.rentmycar_android_app.routing

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NominatimClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}