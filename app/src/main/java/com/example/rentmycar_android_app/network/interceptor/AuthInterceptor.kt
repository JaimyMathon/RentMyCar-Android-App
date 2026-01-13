package com.example.rentmycar_android_app.network.interceptor

import com.example.rentmycar_android_app.data.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get token synchronously (OkHttp interceptors can't be suspend)
        // runBlocking is acceptable here as DataStore is fast for single reads
        val token = runBlocking { tokenManager.getToken() }

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
