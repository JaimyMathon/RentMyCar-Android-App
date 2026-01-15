package com.example.rentmycar_android_app.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * =============================================================================
 * DESIGN PATTERN 1: SINGLETON PATTERN (ook hier toegepast)
 * =============================================================================
 *
 * ApiClient is een Singleton die de Retrofit instantie beheert.
 * 'object' in Kotlin garandeert één enkele instantie in de hele applicatie.
 */
object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)  // Gebruikt automatisch juiste IP
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}