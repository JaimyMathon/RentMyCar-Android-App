package com.example.rentmycar_android_app.network

data class CarCreateRequest(
    val brand: String,
    val model: String,
    val licensePlate: String,
    val category: String,
    val pricePerTimeSlot: Double,
    val costPerKm: Double,
    val latitude: Double,
    val longitude: Double
)