package com.example.rentmycar_android_app.network

data class AuthResponse(
    val username: String,
    val token: String? = null,
    val expiresAt: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val phone: String? = null
)
