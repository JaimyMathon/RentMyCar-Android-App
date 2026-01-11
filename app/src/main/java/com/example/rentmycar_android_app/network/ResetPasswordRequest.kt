package com.example.rentmycar_android_app.network

data class ResetPasswordRequest(
    val email: String,
    val newPassword: String
)
