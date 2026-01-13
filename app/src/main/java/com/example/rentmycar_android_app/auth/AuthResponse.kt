package com.example.rentmycar_android_app.auth

data class AuthResponse(
    val username: String,
    val token: String,
    val expiresAt: String
)