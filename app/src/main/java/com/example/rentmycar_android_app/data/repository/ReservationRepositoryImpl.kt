package com.example.rentmycar_android_app.data.repository

import com.example.rentmycar_android_app.domain.repository.ReservationRepository
import com.example.rentmycar_android_app.network.CreateReservationRequest
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.ReservationService
import com.example.rentmycar_android_app.network.UpdateReservationRequest
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReservationRepositoryImpl @Inject constructor(
    private val reservationService: ReservationService
) : ReservationRepository {

    override suspend fun getReservations(): Result<List<ReservationDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val reservations = reservationService.getReservations()
                Result.Success(reservations)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij ophalen reserveringen: ${e.message}")
            }
        }
    }

    override suspend fun createReservation(request: CreateReservationRequest): Result<ReservationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val reservation = reservationService.createReservation(request)
                Result.Success(reservation)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij aanmaken reservering: ${e.message}")
            }
        }
    }

    override suspend fun updateReservation(
        id: String,
        request: UpdateReservationRequest
    ): Result<ReservationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val reservation = reservationService.updateReservation(id, request)
                Result.Success(reservation)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij updaten reservering: ${e.message}")
            }
        }
    }

    override suspend fun cancelReservation(id: String): Result<ReservationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val reservation = reservationService.cancelReservation(id)
                Result.Success(reservation)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij annuleren reservering: ${e.message}")
            }
        }
    }
}
