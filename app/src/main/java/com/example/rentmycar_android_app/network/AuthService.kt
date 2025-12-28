package com.example.rentmycar_android_app.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("reset-password")
    fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Call<SimpleResponse>
}
