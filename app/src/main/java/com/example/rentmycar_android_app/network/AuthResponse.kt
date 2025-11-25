package com.example.rentmycar_android_app.network

data class AuthResponse(
    val username: String,
    val token: String,
    val expiresAt: String
)
