package com.example.rentmycar_android_app.network

data class CarResponse(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val licensePlate: String? = null
)
