package com.example.rentmycar_android_app.domain.repository

import com.example.rentmycar_android_app.network.AuthResponse
import com.example.rentmycar_android_app.network.RegisterRequest
import com.example.rentmycar_android_app.network.ResetPasswordRequest
import com.example.rentmycar_android_app.network.SimpleResponse
import com.example.rentmycar_android_app.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): Result<SimpleResponse>
    suspend fun logout()
}
