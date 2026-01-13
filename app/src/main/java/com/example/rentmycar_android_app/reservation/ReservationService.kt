package com.example.rentmycar_android_app.reservation

import com.example.rentmycar_android_app.reservation.ReservationDto
import com.example.rentmycar_android_app.network.SimpleResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReservationService {

    @GET("get-reservation")
    suspend fun getReservations(
        @Header("Authorization") token: String,
        @Query("sorting_field") sortingField: String? = null,
        @Query("sorting_direction") sortingDirection: String? = null
    ): Response<List<ReservationDto>>

    @POST("add-reservation")
    suspend fun addReservation(
        @Header("Authorization") token: String,
        @Body reservation: ReservationDto
    ): Response<ReservationDto>

    @PATCH("update-reservation/{id}")
    suspend fun updateReservation(
        @Header("Authorization") token: String,
        @Path("id") reservationId: String,
        @Body reservation: ReservationDto
    ): Response<ReservationDto>

    @PATCH("cancel-reservation/{id}")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("id") reservationId: String
    ): Response<SimpleResponseDto>
}