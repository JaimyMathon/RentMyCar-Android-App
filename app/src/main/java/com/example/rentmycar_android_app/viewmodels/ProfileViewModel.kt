package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model.UserBonus
import com.example.rentmycar_android_app.model.UpdateProfileRequest
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val apiService = ApiClient.instance.create(ApiService::class.java)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _bonus = MutableStateFlow<UserBonus?>(null)
    val bonus: StateFlow<UserBonus?> = _bonus.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfile(token: String?) {
        if (token.isNullOrEmpty()) {
            _error.value = "Geen token gevonden"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                // Laad gebruiker
                val userResponse = apiService.getProfile("Bearer $token")
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val authResponse = userResponse.body()!!
                    _user.value = User(
                        id = authResponse.userId ?: "",
                        name = authResponse.username,
                        email = authResponse.email ?: "",
                        phone = authResponse.phone
                    )
                }

                // Laad bonuspunten (apart)
                try {
                    val bonusResponse = apiService.getBonusPoints("Bearer $token")
                    if (bonusResponse.isSuccessful && bonusResponse.body() != null) {
                        _bonus.value = bonusResponse.body()
                    }
                } catch (e: Exception) {
                    // Ignore bonus errors
                }

            } catch (e: Exception) {
                _error.value = "Netwerkfout: ${e.message}"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun onNameChange(newName: String) {
        _user.value = _user.value?.copy(name = newName)
    }

    fun onEmailChange(newEmail: String) {
        _user.value = _user.value?.copy(email = newEmail)
    }

    fun onPhoneChange(newPhone: String) {
        _user.value = _user.value?.copy(phone = newPhone)
    }

    fun updateProfile(token: String?) {
        if (token.isNullOrEmpty()) {
            _error.value = "Geen token gevonden"
            return
        }

        val currentUser = _user.value
        if (currentUser == null) {
            _error.value = "Geen gebruikersgegevens gevonden"
            return
        }

        if (currentUser.name.isBlank() || currentUser.email.isBlank()) {
            _error.value = "Naam en email zijn verplicht"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val updateRequest = UpdateProfileRequest(
                    name = currentUser.name,
                    email = currentUser.email,
                    phone = currentUser.phone
                )

                val response = apiService.updateProfile("Bearer $token", updateRequest)

                if (response.isSuccessful) {
                    _error.value = "Profiel succesvol bijgewerkt"
                } else {
                    _error.value = "Fout bij opslaan: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Netwerkfout: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
