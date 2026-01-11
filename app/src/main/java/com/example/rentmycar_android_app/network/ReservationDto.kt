package com.example.rentmycar_android_app.network

data class ReservationDto(
    val id: String? = null,
    val carId: String,
    val renterId: String,
    val startTime: String,
    val endTime: String,
    val estimatedDistance: Double,
    val status: String? = null
)
