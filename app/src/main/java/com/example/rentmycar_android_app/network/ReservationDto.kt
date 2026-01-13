package com.example.rentmycar_android_app.network

import com.google.gson.annotations.SerializedName

data class ReservationDto(
    @SerializedName("_id")
    val id: String = "",
    val carId: String? = null,
    val renterId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val estimatedDistance: Double? = null,
    val status: String? = null, // "pending", "confirmed", "cancelled", "completed"
    val car: CarDto? = null, // Nested car object
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ReservationsResponse(
    val reservations: List<ReservationDto>
)
