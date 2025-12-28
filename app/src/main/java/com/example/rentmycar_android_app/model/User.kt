package com.example.rentmycar_android_app.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val password: String = ""
)