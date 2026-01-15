package com.example.rentmycar_android_app.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClientWithToken - Retrofit client met automatische JWT token authenticatie.
 * Gebruikt NetworkConfig voor de juiste BASE_URL (emulator vs fysiek apparaat).
 */
class ApiClientWithToken(private val context: Context) {

    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("jwt_token", "") ?: ""

            val req = chain.request().newBuilder()
                .apply {
                    if (token.isNotBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }
                .build()

            return chain.proceed(req)
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)  // Extra tijd voor file uploads
        .build()

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.BASE_URL)  // Gebruikt automatisch juiste IP
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}