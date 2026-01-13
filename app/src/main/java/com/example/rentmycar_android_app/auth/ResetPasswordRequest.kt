package com.example.rentmycar_android_app.auth

data class ResetPasswordRequest(
    val email: String,
    val newPassword: String
)