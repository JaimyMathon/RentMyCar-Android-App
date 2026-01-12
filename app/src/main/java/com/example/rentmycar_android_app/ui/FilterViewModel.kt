package com.example.rentmycar_android_app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FilterViewModel(
    private val context: Context
) : ViewModel() {

    private val carService: CarService by lazy {
        ApiClientWithToken(context).instance.create(CarService::class.java)
    }

    private val _availableBrands = MutableStateFlow<List<String>>(emptyList())
    val availableBrands: StateFlow<List<String>> = _availableBrands

    init {
        loadAvailableBrands()
    }

    private fun loadAvailableBrands() {
        viewModelScope.launch {
            try {
                val cars = carService.getCars().cars
                val brands = cars
                    .mapNotNull { it.brand }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                _availableBrands.value = brands
            } catch (e: Exception) {
                // If loading fails, provide some default brands
                _availableBrands.value = listOf("Honda", "Nissan", "Audi", "Mercedes")
            }
        }
    }
}

class FilterViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilterViewModel::class.java)) {
            return FilterViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}