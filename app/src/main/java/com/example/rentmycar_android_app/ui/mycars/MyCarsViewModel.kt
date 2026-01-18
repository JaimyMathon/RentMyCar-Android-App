package com.example.rentmycar_android_app.ui.mycars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.data.auth.TokenManager
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.ApiService
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyCarsUiState(
    val isLoading: Boolean = true,
    val cars: List<CarDto> = emptyList(),
    val error: String? = null,
    val currentUserId: String? = null
)

@HiltViewModel
class MyCarsViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyCarsUiState())
    val uiState: StateFlow<MyCarsUiState> = _uiState

    init {
        loadMyCars()
    }

    fun loadMyCars() {
        viewModelScope.launch {
            _uiState.value = MyCarsUiState(isLoading = true)

            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                _uiState.value = MyCarsUiState(isLoading = false, error = "Niet ingelogd")
                return@launch
            }

            try {
                val profileResponse = apiService.getProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val userId = profileResponse.body()?.id
                    if (userId != null) {
                        when (val result = carRepository.getCars()) {
                            is Result.Success -> {
                                val myCars = result.data.filter { car ->
                                    car.addedBy == userId || car.ownerId == userId
                                }
                                _uiState.value = MyCarsUiState(
                                    isLoading = false,
                                    cars = myCars,
                                    currentUserId = userId
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = MyCarsUiState(
                                    isLoading = false,
                                    error = result.message ?: "Fout bij ophalen auto's"
                                )
                            }
                            is Result.Loading -> {}
                        }
                    } else {
                        _uiState.value = MyCarsUiState(
                            isLoading = false,
                            error = "Gebruiker niet gevonden"
                        )
                    }
                } else {
                    _uiState.value = MyCarsUiState(
                        isLoading = false,
                        error = "Kon profiel niet ophalen"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MyCarsUiState(
                    isLoading = false,
                    error = e.message ?: "Onbekende fout"
                )
            }
        }
    }

    fun refresh() {
        loadMyCars()
    }
}
