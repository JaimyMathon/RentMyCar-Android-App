package com.example.rentmycar_android_app.network

data class ProcessPaymentRequest(
    val carId: String,
    val reservationId: String,
    val amount: Double,
    val paymentMethod: String
)
