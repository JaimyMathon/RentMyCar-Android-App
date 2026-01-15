package com.example.rentmycar_android_app.network

data class AddCarRequest(
    val ownerId: String,
    val brand: String,
    val model: String,
    val licensePlate: String,
    val category: String,
    val pricePerTimeSlot: Double,
    val latitude: Double,
    val longitude: Double,
    val costPerKm: Double,
    val fuelCost: Double,
    val maintenance: Double,
    val insurance: Double,
    val depreciation: Double
)
