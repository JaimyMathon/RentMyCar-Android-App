package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CarDetailUiState(
    val isLoading: Boolean = true,
    val car: CarDto? = null,
    val error: String? = null
)

class CarDetailViewModel(
    private val context: Context,
    private val carId: String
) : ViewModel() {

    private val carService: CarService by lazy {
        ApiClientWithToken(context).instance.create(CarService::class.java)
    }

    private val _uiState = MutableStateFlow(CarDetailUiState())
    val uiState: StateFlow<CarDetailUiState> = _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CarDetailUiState(isLoading = true)
            try {
                val car = carService.getCarById(carId)
                _uiState.value = CarDetailUiState(isLoading = false, car = car, error = null)
            } catch (e: Exception) {
                _uiState.value = CarDetailUiState(
                    isLoading = false,
                    car = null,
                    error = e.message ?: "Fout bij ophalen auto"
                )
            }
        }
    }
}

class CarDetailViewModelFactory(
    private val context: Context,
    private val carId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarDetailViewModel::class.java)) {
            return CarDetailViewModel(context.applicationContext, carId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}