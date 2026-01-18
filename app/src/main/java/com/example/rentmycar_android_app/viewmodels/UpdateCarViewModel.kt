package com.example.rentmycar_android_app.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateCarUiState(
    val isLoading: Boolean = true,
    val car: CarDto? = null,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val updateSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val isGeocodingInProgress: Boolean = false,
    val geocodingError: String? = null,
    val newLatitude: Double? = null,
    val newLongitude: Double? = null
)

@HiltViewModel
class UpdateCarViewModel @Inject constructor(
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: String = checkNotNull(savedStateHandle["carId"])

    private val _uiState = MutableStateFlow(UpdateCarUiState())
    val uiState: StateFlow<UpdateCarUiState> = _uiState

    init {
        loadCar()
    }

    fun loadCar() {
        viewModelScope.launch {
            _uiState.value = UpdateCarUiState(isLoading = true)

            when (val result = carRepository.getCarById(carId)) {
                is Result.Success -> {
                    _uiState.value = UpdateCarUiState(
                        isLoading = false,
                        car = result.data,
                        newLatitude = result.data.latitude,
                        newLongitude = result.data.longitude
                    )
                }
                is Result.Error -> {
                    _uiState.value = UpdateCarUiState(
                        isLoading = false,
                        error = result.message ?: "Fout bij laden auto"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun geocodeAddress(street: String, houseNumber: String, postcode: String, country: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeocodingInProgress = true,
                geocodingError = null
            )

            val fullStreet = if (houseNumber.isNotBlank()) "$street $houseNumber" else street

            when (val result = carRepository.geocodeAddress(fullStreet, postcode, country)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isGeocodingInProgress = false,
                        newLatitude = result.data.first,
                        newLongitude = result.data.second,
                        geocodingError = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeocodingInProgress = false,
                        geocodingError = result.message ?: "Adres niet gevonden"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun updateCar(
        brand: String,
        model: String,
        licensePlate: String,
        category: String,
        pricePerTimeSlot: Double,
        costPerKm: Double,
        fuelCost: Double,
        maintenance: Double,
        insurance: Double,
        depreciation: Double
    ) {
        val lat = _uiState.value.newLatitude ?: _uiState.value.car?.latitude ?: 0.0
        val lon = _uiState.value.newLongitude ?: _uiState.value.car?.longitude ?: 0.0
        val ownerId = _uiState.value.car?.ownerId ?: _uiState.value.car?.addedBy ?: ""

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

            val request = AddCarRequest(
                ownerId = ownerId,
                brand = brand,
                model = model,
                licensePlate = licensePlate,
                category = category,
                pricePerTimeSlot = pricePerTimeSlot,
                latitude = lat,
                longitude = lon,
                costPerKm = costPerKm,
                fuelCost = fuelCost,
                maintenance = maintenance,
                insurance = insurance,
                depreciation = depreciation
            )

            when (val result = carRepository.updateCar(carId, request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = result.message ?: "Fout bij bijwerken auto"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteCar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)

            when (val result = carRepository.deleteCar(carId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteSuccess = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = result.message ?: "Fout bij verwijderen auto"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, geocodingError = null)
    }
}
