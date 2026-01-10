package com.example.rentmycar_android_app.network

import com.google.gson.annotations.SerializedName

// Pas de veldnamen aan jouw echte API-response aan
data class CarDto(
    val id: String,
    val brand: String,
    val model: String,
    val bodyType: String?,     // Sedan, SUV, ...
    val city: String?,
    val category: String?,     // ICE, BEV, FCEV
    val fuelType: String?,     // ICE, EV, Hybrid, ... (deprecated, use category)

    @SerializedName("pricePerTimeSlot")
    val pricePerDay: Double,   // Maps to pricePerTimeSlot in database

    @SerializedName("costPerKm")
    val pricePerKm: Double?,   // Maps to costPerKm in database

    val rating: Double?,       // 4.8
    val imageUrl: String?,     // voor later, nu placeholder
    val latitude: Double,
    val longitude: Double
)