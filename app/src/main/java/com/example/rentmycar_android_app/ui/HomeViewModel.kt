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

data class FilterState(
    val selectedTypes: Set<String> = setOf("All"),
    val pricePerKmRange: ClosedFloatingPointRange<Float> = 0.0f..0.5f,
    val pricePerDayRange: ClosedFloatingPointRange<Float> = 0f..500f,
    val selectedBrands: Set<String> = setOf("All")
) {
    fun isActive(): Boolean {
        return selectedTypes != setOf("All") ||
                pricePerKmRange != 0.0f..0.5f ||
                pricePerDayRange != 0f..500f ||
                selectedBrands != setOf("All")
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val cars: List<CarDto> = emptyList(),
    val filteredCars: List<CarDto> = emptyList(),
    val availableBrands: List<String> = emptyList(),
    val error: String? = null,
    val filterState: FilterState = FilterState(),
    val searchQuery: String = ""
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
                val cars = carService.getCars().cars

                // Extract unique brands from the car list - take only the first word
                val uniqueBrands = cars.map {
                    it.brand.split(" ").firstOrNull() ?: it.brand
                }.distinct().sorted()

                // Log price ranges for debugging
                val minPricePerKm = cars.mapNotNull { it.pricePerKm }.minOrNull() ?: 0.0
                val maxPricePerKm = cars.mapNotNull { it.pricePerKm }.maxOrNull() ?: 0.0
                val minPricePerDay = cars.map { it.pricePerDay }.minOrNull() ?: 0.0
                val maxPricePerDay = cars.map { it.pricePerDay }.maxOrNull() ?: 0.0

                android.util.Log.d("HomeViewModel", "Loaded ${cars.size} cars")
                android.util.Log.d("HomeViewModel", "Brands: $uniqueBrands")
                android.util.Log.d("HomeViewModel", "Price per km range: €$minPricePerKm - €$maxPricePerKm")
                android.util.Log.d("HomeViewModel", "Price per day range: €$minPricePerDay - €$maxPricePerDay")

                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = cars,
                    filteredCars = cars,
                    availableBrands = uniqueBrands,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    cars = emptyList(),
                    filteredCars = emptyList(),
                    availableBrands = emptyList(),
                    error = e.message ?: "Onbekende fout bij ophalen auto's"
                )
            }
        }
    }

    fun updateFilters(newFilterState: FilterState) {
        val filteredCars = applyFiltersAndSearch(_uiState.value.cars, newFilterState, _uiState.value.searchQuery)
        _uiState.value = _uiState.value.copy(
            filterState = newFilterState,
            filteredCars = filteredCars
        )
    }

    fun resetFilters() {
        val defaultFilter = FilterState()
        val filteredCars = applyFiltersAndSearch(_uiState.value.cars, defaultFilter, _uiState.value.searchQuery)
        _uiState.value = _uiState.value.copy(
            filterState = defaultFilter,
            filteredCars = filteredCars
        )
    }

    fun updateSearchQuery(query: String) {
        val filteredCars = applyFiltersAndSearch(_uiState.value.cars, _uiState.value.filterState, query)
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredCars = filteredCars
        )
    }

    private fun applyFiltersAndSearch(cars: List<CarDto>, filterState: FilterState, searchQuery: String): List<CarDto> {
        android.util.Log.d("HomeViewModel", "Starting filter with: types=${filterState.selectedTypes}, brands=${filterState.selectedBrands}, pricePerKm=${filterState.pricePerKmRange}, pricePerDay=${filterState.pricePerDayRange}, search='$searchQuery'")

        val filteredList = cars.filter { car ->
            // Search filter - matches brand, model, or city
            val searchMatch = if (searchQuery.isBlank()) {
                true
            } else {
                val query = searchQuery.lowercase()
                car.brand.lowercase().contains(query) ||
                car.model.lowercase().contains(query) ||
                (car.city?.lowercase()?.contains(query) ?: false)
            }

            val typeMatch = if ("All" in filterState.selectedTypes) {
                true
            } else {
                // Use category field directly (already contains ICE, BEV, FCEV)
                val carType = car.category?.uppercase() ?: "ICE"
                carType in filterState.selectedTypes
            }

            val pricePerKmMatch = car.pricePerKm?.let {
                val inRange = it.toFloat() in filterState.pricePerKmRange
                android.util.Log.d("HomeViewModel", "  pricePerKm check: ${it.toFloat()} in ${filterState.pricePerKmRange} = $inRange")
                inRange
            } ?: true

            val pricePerDayValue = car.pricePerDay.toFloat()
            val pricePerDayMatch = pricePerDayValue in filterState.pricePerDayRange
            android.util.Log.d("HomeViewModel", "  pricePerDay check: $pricePerDayValue in ${filterState.pricePerDayRange} = $pricePerDayMatch")

            val brandMatch = if ("All" in filterState.selectedBrands) {
                true
            } else {
                // Extract first word from brand for comparison
                val carBrandFirstWord = car.brand.split(" ").firstOrNull() ?: car.brand
                carBrandFirstWord in filterState.selectedBrands
            }

            val passes = searchMatch && typeMatch && pricePerKmMatch && pricePerDayMatch && brandMatch

            if (!passes) {
                android.util.Log.d("HomeViewModel", "Car filtered out: ${car.brand} ${car.model} - search=$searchMatch, type=$typeMatch, pricePerKm=$pricePerKmMatch (${car.pricePerKm}), pricePerDay=$pricePerDayMatch (${car.pricePerDay}), brand=$brandMatch")
            }

            passes
        }

        android.util.Log.d("HomeViewModel", "Filtering result: ${cars.size} cars -> ${filteredList.size} cars")

        return filteredList
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