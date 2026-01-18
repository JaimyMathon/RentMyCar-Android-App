package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.ui.FilterState
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val cars: List<CarDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

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

            when (val result = carRepository.getCars()) {
                is Result.Success -> {
                    allCars = result.data

                    _uiState.value = HomeUiState(
                        isLoading = false,
                        cars = applySearch(applyFilters(result.data, currentFilter), searchQuery),
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        cars = emptyList(),
                        error = result.message ?: "Onbekende fout bij ophalen auto's"
                    )
                }
                is Result.Loading -> {
                    // Already loading
                }
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

