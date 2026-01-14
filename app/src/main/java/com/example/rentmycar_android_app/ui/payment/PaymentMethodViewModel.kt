package com.example.rentmycar_android_app.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.PaymentRepository
import com.example.rentmycar_android_app.domain.repository.ReservationRepository
import com.example.rentmycar_android_app.domain.repository.UserRepository
import com.example.rentmycar_android_app.network.CreateReservationRequest
import com.example.rentmycar_android_app.network.ProcessPaymentRequest
import com.example.rentmycar_android_app.ui.PaymentMethod
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class PaymentMethodUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val paymentSuccess: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val carId: String = savedStateHandle["carId"] ?: ""
    val fromDate: String = savedStateHandle["fromDate"] ?: ""
    val toDate: String = savedStateHandle["toDate"] ?: ""
    val kms: String = savedStateHandle["kms"] ?: "0"
    val amount: Double = (savedStateHandle.get<String>("amount") ?: "0").toDoubleOrNull() ?: 0.0

    private val _uiState = MutableStateFlow(PaymentMethodUiState())
    val uiState: StateFlow<PaymentMethodUiState> = _uiState.asStateFlow()

    fun processPayment(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            _uiState.value = PaymentMethodUiState(isLoading = true)

            // First get the user ID
            val userResult = userRepository.getProfile()
            val userId = when (userResult) {
                is Result.Success -> userResult.data.id
                is Result.Error -> {
                    _uiState.value = PaymentMethodUiState(
                        error = userResult.message ?: "Kon gebruiker niet ophalen"
                    )
                    return@launch
                }
                is Result.Loading -> return@launch
            }

            // Convert date format from dd-MM-yyyy to ISO format for kotlinx.datetime.LocalDateTime
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            val startTimeFormatted = try {
                val date = inputFormat.parse(fromDate)
                if (date != null) outputFormat.format(date) else fromDate
            } catch (e: Exception) {
                fromDate
            }

            val endTimeFormatted = try {
                val date = inputFormat.parse(toDate)
                if (date != null) outputFormat.format(date) else toDate
            } catch (e: Exception) {
                toDate
            }

            // Step 1: Create reservation
            val createRequest = CreateReservationRequest(
                carId = carId,
                renterId = userId,
                startTime = startTimeFormatted,
                endTime = endTimeFormatted,
                estimatedDistance = kms.toDoubleOrNull() ?: 0.0
            )

            when (val reservationResult = reservationRepository.createReservation(createRequest)) {
                is Result.Success -> {
                    val reservation = reservationResult.data

                    // Step 2: Process payment (backend only needs the reservation ID)
                    val paymentRequest = ProcessPaymentRequest(
                        id = reservation.id
                    )

                    when (val paymentResult = paymentRepository.processPayment(paymentRequest)) {
                        is Result.Success -> {
                            _uiState.value = PaymentMethodUiState(
                                paymentSuccess = true,
                                successMessage = paymentResult.data.message ?: "Betaling succesvol!"
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = PaymentMethodUiState(
                                error = paymentResult.message ?: "Fout bij verwerken betaling"
                            )
                        }
                        is Result.Loading -> {
                            // Already showing loading state
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.value = PaymentMethodUiState(
                        error = reservationResult.message ?: "Fout bij aanmaken reservering"
                    )
                }
                is Result.Loading -> {
                    // Already showing loading state
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
