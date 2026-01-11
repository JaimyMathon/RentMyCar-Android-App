package com.example.rentmycar_android_app.data

import kotlinx.serialization.Serializable

@Serializable
data class SimpleEmailRequest(
    val email: String
)
