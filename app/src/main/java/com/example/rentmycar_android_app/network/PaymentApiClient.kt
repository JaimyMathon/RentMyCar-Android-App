package com.example.rentmycar_android_app.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PaymentApiClient {

    fun createService(token: String?): PaymentService {
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
            .baseUrl(NetworkConfig.BASE_URL)  // Gebruikt centrale configuratie
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(PaymentService::class.java)
    }
}