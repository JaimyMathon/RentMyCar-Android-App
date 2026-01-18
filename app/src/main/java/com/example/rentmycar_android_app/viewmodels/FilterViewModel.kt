package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.ui.FilterState
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    init {
        loadAvailableBrands()
    }

    fun initializeFilter(initialState: FilterState) {
        _filterState.value = initialState
    }

    fun updateSelectedTypes(types: Set<String>) {
        _filterState.update { it.copy(selectedTypes = types) }
    }

    fun updateMaxPricePerKm(price: Float) {
        _filterState.update { it.copy(maxPricePerKm = price) }
    }

    fun updateMaxPricePerDay(price: Float) {
        _filterState.update { it.copy(maxPricePerDay = price) }
    }

    fun updateSelectedBrands(brands: Set<String>) {
        _filterState.update { it.copy(selectedBrands = brands) }
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun getCurrentFilterState(): FilterState {
        return _filterState.value
    }

    private fun loadAvailableBrands() {
        viewModelScope.launch {
            when (val result = carRepository.getCars()) {
                is Result.Success -> {
                    val brands = result.data
                        .mapNotNull { it.brand }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()

                    _availableBrands.value = brands
                }
                is Result.Error -> {
                    _availableBrands.value = listOf("Honda", "Nissan", "Audi", "Mercedes")
                }
                is Result.Loading -> {
                    // Loading
                }
            }
        }
    }
}