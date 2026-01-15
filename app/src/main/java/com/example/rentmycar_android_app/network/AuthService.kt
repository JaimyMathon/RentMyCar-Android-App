package com.example.rentmycar_android_app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ResetPasswordRequest): Response<SimpleResponseDto>
}
