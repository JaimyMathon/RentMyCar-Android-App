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

class HomeViewModel(
    private val context: Context
) : ViewModel() {

    // Gebruik ApiClientWithToken om CarService aan te maken
    private val carService: CarService by lazy {
        ApiClientWithToken(context).instance.create(CarService::class.java)
    }

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    private var allCars: List<CarDto> = emptyList()
    private var currentFilter: FilterState? = null
    private var searchQuery: String = ""

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            try {
                val cars = carService.getCars().cars   // <<< HIER ZIT DE FIX
                allCars = cars

                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = applySearch(applyFilters(cars, currentFilter), searchQuery),
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

    fun applyFilter(filterState: FilterState) {
        currentFilter = filterState
        updateCarsList()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        updateCarsList()
    }

    fun getCurrentFilter(): FilterState {
        return currentFilter ?: FilterState()
    }

    private fun updateCarsList() {
        val filtered = applyFilters(allCars, currentFilter)
        val searched = applySearch(filtered, searchQuery)
        _uiState.value = _uiState.value.copy(cars = searched)
    }

    private fun applyFilters(cars: List<CarDto>, filter: FilterState?): List<CarDto> {
        if (filter == null) return cars

        return cars.filter { car ->
            // Filter by type/category
            val categoryMatch = filter.selectedTypes.isEmpty() ||
                    filter.selectedTypes.any { type ->
                        car.category.equals(type, ignoreCase = true)
                    }

            // Filter by price per km (max price filter)
            val pricePerKmMatch = car.costPerKm?.let { cost ->
                cost <= filter.maxPricePerKm
            } ?: true

            // Filter by price per day (max price filter)
            val pricePerDayMatch = car.pricePerTimeSlot?.let { price ->
                price <= filter.maxPricePerDay
            } ?: true

            // Filter by brand
            val brandMatch = filter.selectedBrands.isEmpty() ||
                    filter.selectedBrands.any { brand ->
                        car.brand.equals(brand, ignoreCase = true)
                    }

            categoryMatch && pricePerKmMatch && pricePerDayMatch && brandMatch
        }
    }

    private fun applySearch(cars: List<CarDto>, query: String): List<CarDto> {
        if (query.isBlank()) return cars

        val lowerQuery = query.lowercase().trim()

        return cars.filter { car ->
            val brand = car.brand?.lowercase() ?: ""
            val model = car.model?.lowercase() ?: ""
            val category = car.category?.lowercase() ?: ""

            brand.contains(lowerQuery) ||
            model.contains(lowerQuery) ||
            category.contains(lowerQuery) ||
            "$brand $model".contains(lowerQuery)
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