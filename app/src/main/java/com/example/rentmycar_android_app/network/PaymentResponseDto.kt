package com.example.rentmycar_android_app.network

data class PaymentResponseDto(
    val paymentId: String,
    val status: String,
    val message: String? = null
)
