package com.example.rentmycar_android_app.domain.repository

import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.UpdateReservationRequest
import com.example.rentmycar_android_app.util.Result

interface ReservationRepository {
    suspend fun getReservations(): Result<List<ReservationDto>>
    suspend fun updateReservation(id: String, request: UpdateReservationRequest): Result<ReservationDto>
    suspend fun cancelReservation(id: String): Result<ReservationDto>
}
