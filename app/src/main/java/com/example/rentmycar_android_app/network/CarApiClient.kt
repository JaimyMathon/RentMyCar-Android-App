package com.example.rentmycar_android_app.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CarApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    fun createService(token: String?): CarService {
        val clientBuilder = OkHttpClient.Builder()

        if (!token.isNullOrBlank()) {
            val authInterceptor = Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }
            clientBuilder.addInterceptor(authInterceptor)
        }

        val client = clientBuilder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(CarService::class.java)
    }
}