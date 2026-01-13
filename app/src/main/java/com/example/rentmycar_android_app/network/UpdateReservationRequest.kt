package com.example.rentmycar_android_app.network

data class UpdateReservationRequest(
    val fromDate: String? = null,
    val toDate: String? = null,
    val kms: Int? = null
)
