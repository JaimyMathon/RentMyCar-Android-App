package com.example.rentmycar_android_app.network

// Pas de veldnamen aan jouw echte API-response aan
import com.google.gson.annotations.SerializedName

data class CarDto(
    @SerializedName("_id")
    val id: String = "",
    val ownerId: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val licensePlate: String? = null,
    val category: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pricePerTimeSlot: Double? = null,
    val tco: Double? = null,
    val costPerKm: Double? = null,
    val fuelCost: Double? = null,
    val maintenance: Double? = null,
    val insurance: Double? = null,
    val depreciation: Double? = null,
    val status: String? = null,
    @SerializedName("added_by")
    val addedBy: String? = null,
    val imageUrl: String? = null
)