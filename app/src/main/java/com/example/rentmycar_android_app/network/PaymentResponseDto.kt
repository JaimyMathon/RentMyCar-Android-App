package com.example.rentmycar_android_app.network

data class PaymentResponseDto(
    val isSuccess: Boolean,
    val message: String? = null,
    val payment: PaymentDetailDto? = null
)

data class PaymentDetailDto(
    val id: String? = null,
    val reservationId: String? = null,
    val carId: String? = null,
    val renterId: String? = null,
    val baseRentalCost: Double? = null,
    val tcoCost: Double? = null,
    val distanceCost: Double? = null,
    val totalAmount: Double? = null,
    val status: String? = null
)
