package com.example.rentmycar_android_app.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClientWithToken(context: Context) {
    private val BASE_URL = "http://10.0.2.2:8080/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)

            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
