package com.example.rentmycar_android_app.core.model

data class DrivingBehavior(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val carId: String? = null,
    val carName: String? = null,
    val timestamp: Long = 0L,
    val maxAccelerationForce: Double = 0.0,
    val maxBrakingForce: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val distance: Double = 0.0,
    val duration: Long = 0L,
    val harshAccelerationCount: Int = 0,
    val harshBrakingCount: Int = 0,
    val points: Int = 0,
    val rating: String = ""
)

data class DrivingDataRequest(
    val maxAccelerationForce: Double,
    val maxBrakingForce: Double,
    val avgSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val distance: Double = 0.0,
    val duration: Long,
    val harshAccelerationCount: Int = 0,
    val harshBrakingCount: Int = 0,
    val carId: String? = null,
    val carName: String? = null
)

data class DrivingStatsResponse(
    val totalPoints: Int,
    val averageRating: String,
    val totalTrips: Int,
    val totalDistance: Double,
    val behaviors: List<DrivingBehavior>
)