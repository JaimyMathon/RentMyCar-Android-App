package com.example.rentmycar_android_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    init {
        loadAvailableBrands()
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
                    // If loading fails, provide some default brands
                    _availableBrands.value = listOf("Honda", "Nissan", "Audi", "Mercedes")
                }
                is Result.Loading -> {
                    // Loading
                }
            }
        }
    }
}