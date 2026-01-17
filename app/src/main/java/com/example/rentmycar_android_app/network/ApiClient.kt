package com.example.rentmycar_android_app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * DEPRECATED: Tijdelijke backwards compatibility stub.
 * Gebruik Hilt dependency injection in plaats van deze class.
 */
@Deprecated("Use Hilt dependency injection instead")
object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8081/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
