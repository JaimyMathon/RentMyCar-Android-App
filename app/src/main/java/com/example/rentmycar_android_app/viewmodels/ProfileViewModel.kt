package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.UserRepository
import com.example.rentmycar_android_app.model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model.UserBonus
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _bonus = MutableStateFlow<UserBonus?>(null)
    val bonus: StateFlow<UserBonus?> = _bonus.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            // Load user profile
            when (val userResult = userRepository.getProfile()) {
                is Result.Success -> {
                    _user.value = userResult.data
                }
                is Result.Error -> {
                    _error.value = userResult.message
                }
                is Result.Loading -> {}
            }

            // Load bonus points
            when (val bonusResult = userRepository.getBonusPoints()) {
                is Result.Success -> {
                    _bonus.value = bonusResult.data
                }
                is Result.Error -> {
                    // Don't overwrite user error if present
                    if (_error.value == null) {
                        _error.value = bonusResult.message
                    }
                }
                is Result.Loading -> {}
            }

            _loading.value = false
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

    fun updateProfile() {
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

            val updateRequest = UpdateProfileRequest(
                name = currentUser.name,
                email = currentUser.email,
                phone = currentUser.phone
            )

            when (val result = userRepository.updateProfile(updateRequest)) {
                is Result.Success -> {
                    _error.value = "Profiel succesvol bijgewerkt"
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {}
            }

            _loading.value = false
        }
    }
}
