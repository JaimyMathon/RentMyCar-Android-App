package com.example.rentmycar_android_app.model

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val phone: String? = null
)
