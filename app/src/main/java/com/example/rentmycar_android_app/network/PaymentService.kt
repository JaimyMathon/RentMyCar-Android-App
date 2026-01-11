package com.example.rentmycar_android_app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentService {

    @GET("get-payment/{id}")
    suspend fun getPayment(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<List<PaymentDto>>    // als je API één object terugstuurt: Response<PaymentDto>

    @POST("process-payment")
    suspend fun processPayment(
        @Header("Authorization") token: String,
        @Body request: ProcessPaymentRequest
    ): Response<PaymentResponseDto>
}