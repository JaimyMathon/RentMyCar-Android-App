package com.example.rentmycar_android_app.auth

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)