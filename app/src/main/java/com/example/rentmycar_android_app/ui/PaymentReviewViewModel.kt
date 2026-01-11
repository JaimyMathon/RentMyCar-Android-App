package com.example.rentmycar_android_app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Eenvoudige UI-state, later kun je hier velden uit je PaymentDto aan koppelen
data class PaymentReviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class PaymentReviewViewModel : ViewModel() {

    var uiState by mutableStateOf(PaymentReviewUiState())
        private set

    // placeholders voor later; nu nog geen API-calls
    fun loadPayment(token: String, paymentId: String) {
        // TODO: straks PaymentApiClient.createService(token) gebruiken en data ophalen
    }

    fun processPayment(token: String, reservationId: String, onSuccess: () -> Unit) {
        // TODO: straks API-call naar /process-payment doen en onSuccess() aanroepen bij succes
    }
}