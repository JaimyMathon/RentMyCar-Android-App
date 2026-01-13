package com.example.rentmycar_android_app.domain.repository

import com.example.rentmycar_android_app.network.PaymentDto
import com.example.rentmycar_android_app.network.PaymentResponseDto
import com.example.rentmycar_android_app.network.ProcessPaymentRequest
import com.example.rentmycar_android_app.util.Result

interface PaymentRepository {
    suspend fun getPayment(id: String): Result<List<PaymentDto>>
    suspend fun processPayment(request: ProcessPaymentRequest): Result<PaymentResponseDto>
}
