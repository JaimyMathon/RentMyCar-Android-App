package com.example.rentmycar_android_app.car

import com.google.gson.annotations.SerializedName

data class CarDto(
    @SerializedName("_id")
    val id: String = "",
    val brand: String? = null,
    val model: String? = null,
    val pricePerTimeSlot: Double? = null,
    val costPerKm: Double? = null,
    val category: String? = null,
    val status: String? = null,
    val tco: Double? = null,
    val addedBy: String? = null,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double
)