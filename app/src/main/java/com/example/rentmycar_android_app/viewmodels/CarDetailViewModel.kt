package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CarDetailUiState(
    val isLoading: Boolean = true,
    val car: CarDto? = null,
    val error: String? = null
)

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: String = checkNotNull(savedStateHandle["carId"])

    private val _uiState = MutableStateFlow(CarDetailUiState())
    val uiState: StateFlow<CarDetailUiState> = _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CarDetailUiState(isLoading = true)

            when (val result = carRepository.getCarById(carId)) {
                is Result.Success -> {
                    _uiState.value = CarDetailUiState(
                        isLoading = false,
                        car = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = CarDetailUiState(
                        isLoading = false,
                        car = null,
                        error = result.message ?: "Fout bij ophalen auto"
                    )
                }
                is Result.Loading -> {
                    // Already loading
                }
            }
        }
    }
}

