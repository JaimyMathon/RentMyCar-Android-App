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

data class HomeUiState(
    val isLoading: Boolean = false,
    val cars: List<CarDto> = emptyList(),
    val error: String? = null
)

class HomeViewModel(private val context: Context) : ViewModel() {

    private val carService: CarService by lazy {
        ApiClientWithToken(context).instance.create(CarService::class.java)
    }

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    private var allCars: List<CarDto> = emptyList()
    private var searchQuery: String = ""

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                val cars = carService.getCars().cars
                // Reverse zodat nieuwste auto's bovenaan staan
                allCars = cars.reversed()
                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = applySearch(allCars, searchQuery),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = emptyList(),
                    error = e.message ?: "Onbekende fout"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        _uiState.value = _uiState.value.copy(cars = applySearch(allCars, searchQuery))
    }

    private fun applySearch(cars: List<CarDto>, query: String): List<CarDto> {
        if (query.isBlank()) return cars
        val q = query.lowercase().trim()
        return cars.filter { car ->
            (car.brand?.lowercase()?.contains(q) == true) ||
                    (car.model?.lowercase()?.contains(q) == true) ||
                    (car.category?.lowercase()?.contains(q) == true)
        }
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}