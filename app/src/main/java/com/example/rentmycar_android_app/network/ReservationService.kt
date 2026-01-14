package com.example.rentmycar_android_app.network

import retrofit2.http.*

interface ReservationService {

    @GET("get-reservation")
    suspend fun getReservations(): List<ReservationDto>

    @GET("get-reservations-by-car/{carId}")
    suspend fun getReservationsByCarId(@Path("carId") carId: String): List<ReservationDto>

    @POST("add-reservation")
    suspend fun createReservation(
        @Body request: CreateReservationRequest
    ): ReservationDto

    @PATCH("update-reservation/{id}")
    suspend fun updateReservation(
        @Path("id") id: String,
        @Body request: UpdateReservationRequest
    ): ReservationDto

    @PATCH("cancel-reservation/{id}")
    suspend fun cancelReservation(@Path("id") id: String): ReservationDto
}
