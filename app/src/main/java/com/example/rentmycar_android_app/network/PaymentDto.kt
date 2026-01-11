package com.example.rentmycar_android_app.network

// LET OP: pas de velden/naamgeving aan je echte Payment-model aan
data class PaymentDto(
    val id: String,
    val carName: String? = null,
    val pricePerDay: Double? = null,      // €300 / dag
    val currency: String? = "€",
    val pickupTime: String? = null,       // bv. "Oct 04 | 10:00 AM"
    val dropoffTime: String? = null,      // bv. "Oct 07 | 10:00 AM"
    val totalHours: Int? = null,
    val basePrice: Double? = null,        // bv. 900
    val tcoCost: Double? = null,          // bv. 28.38
    val distanceCost: Double? = null,     // bv. 100
    val totalAmount: Double? = null       // bv. 1028.38
)

data class ProcessPaymentRequest(
    val id: String        // zelfde als PaymentRequest in je backend
)

data class PaymentResponseDto(
    val isSuccess: Boolean,
    val message: String,
    val payment: PaymentDto? = null
)