package com.example.rentmycar_android_app.network

// Pas de veldnamen aan jouw echte API-response aan
data class CarDto(
    val id: String,
    val brand: String,
    val model: String,
    val bodyType: String?,     // Sedan, SUV, ...
    val city: String?,
    val fuelType: String?,     // ICE, EV, Hybrid, ...
    val pricePerDay: Double,
    val pricePerKm: Double?,
    val rating: Double?,       // 4.8
    val imageUrl: String?      // voor later, nu placeholder
)