package com.example.rentmycar_android_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.api.ApiService
import com.example.rentmycar_android_app.data.ResetPasswordWithTokenRequest
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val api: ApiService, token1: String) : ViewModel() {

    var token by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var message by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    init {
        token = ""
    }

    fun resetPassword(onSuccess: () -> Unit) {
        if (newPassword != confirmPassword) {
            message = "Wachtwoorden komen niet overeen"
            return
        }

        if (token.isBlank()) {
            message = "Voer een geldige token in"
            return
        }

        isLoading = true
        message = ""

        viewModelScope.launch {
            try {
                val res = api.resetPassword(
                    ResetPasswordWithTokenRequest(token, newPassword)
                )
                isLoading = false
                if (res.isSuccess) {
                    message = "Wachtwoord succesvol gewijzigd!"
                    onSuccess()
                } else {
                    message = res.message
                }
            } catch (e: Exception) {
                isLoading = false
                message = "Er ging iets mis: ${e.message}"
            }
        }
    }
}

class ResetPasswordViewModelFactory(
    private val api: ApiService,
    private val token: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java)) {
            return ResetPasswordViewModel(api, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

