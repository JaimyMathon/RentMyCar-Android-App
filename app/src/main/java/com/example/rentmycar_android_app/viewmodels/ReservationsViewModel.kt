package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.domain.repository.ReservationRepository
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservationsUiState(
    val isLoading: Boolean = false,
    val reservations: List<ReservationDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState(isLoading = true))
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = reservationRepository.getReservations()) {
                is Result.Success -> {
                    val reservationsWithCars = result.data.map { reservation ->
                        if (reservation.car == null && reservation.carId != null) {
                            when (val carResult = carRepository.getCarById(reservation.carId)) {
                                is Result.Success -> reservation.copy(car = carResult.data)
                                else -> reservation
                            }
                        } else {
                            reservation
                        }
                    }
                    _uiState.value = ReservationsUiState(
                        isLoading = false,
                        reservations = reservationsWithCars,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = ReservationsUiState(
                        isLoading = false,
                        reservations = emptyList(),
                        error = result.message ?: "Fout bij ophalen reserveringen"
                    )
                }
                is Result.Loading -> {
                    // Already loading
                }
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            when (val result = reservationRepository.cancelReservation(reservationId)) {
                is Result.Success -> {
                    loadReservations()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Fout bij annuleren reservering"
                    )
                }
                is Result.Loading -> {
                    // Do nothing
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    suspend fun getCarPhotoUrl(carId: String): String? {
        return when (val result = carRepository.getCarPhotos(carId)) {
            is Result.Success -> {
                if (result.data.isNotEmpty()) {
                    "http://10.0.2.2:8080${result.data[0].url}"
                } else null
            }
            else -> null
        }
    }
}