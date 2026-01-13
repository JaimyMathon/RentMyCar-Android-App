package com.example.rentmycar_android_app.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.core.network.ApiClientWithToken
import com.example.rentmycar_android_app.car.CarDto
import com.example.rentmycar_android_app.car.CarService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val cars: List<CarDto> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val context: Context
) : ViewModel() {

    // Gebruik ApiClientWithToken om CarService aan te maken
    private val carService: CarService by lazy {
        ApiClientWithToken(context).instance.create(CarService::class.java)
    }

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            try {
                val cars = carService.getCars().cars   // <<< HIER ZIT DE FIX

                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = cars,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = emptyList(),
                    error = e.message ?: "Onbekende fout bij ophalen auto's"
                )
            }
        }
    }
}

class HomeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}