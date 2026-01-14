package com.example.rentmycar_android_app.network

data class CreateReservationRequest(
    val carId: String,
    val renterId: String,
    val startTime: String,
    val endTime: String,
    val estimatedDistance: Double
)
