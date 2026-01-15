package com.example.rentmycar_android_app.ui.addcar

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentmycar_android_app.data.auth.TokenManager
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.ApiService
import com.example.rentmycar_android_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AddCarUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isGeocodingInProgress: Boolean = false,
    val geocodingError: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userId: String? = null
)

@HiltViewModel
class AddCarViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    init {
        loadUserId()
    }

    private fun loadUserId() {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (!token.isNullOrEmpty()) {
                try {
                    val response = apiService.getProfile("Bearer $token")
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(userId = response.body()?.id)
                    }
                } catch (e: Exception) {
                    // Ignore, will show error when trying to add car
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(AddCarUiState())
    val uiState: StateFlow<AddCarUiState> = _uiState

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
                        latitude = result.data.first,
                        longitude = result.data.second,
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

    fun addCar(
        brand: String,
        model: String,
        licensePlate: String,
        category: String,
        pricePerTimeSlot: Double,
        costPerKm: Double,
        fuelCost: Double,
        maintenance: Double,
        insurance: Double,
        depreciation: Double,
        photoUri: Uri?,
        context: Context
    ) {
        val lat = _uiState.value.latitude
        val lon = _uiState.value.longitude
        val userId = _uiState.value.userId

        if (lat == null || lon == null) {
            _uiState.value = _uiState.value.copy(
                error = "Zoek eerst het adres op om de locatie te bepalen"
            )
            return
        }

        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Gebruiker niet gevonden. Probeer opnieuw in te loggen."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = AddCarRequest(
                ownerId = userId,
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

            when (val carResult = carRepository.addCar(request)) {
                is Result.Success -> {
                    val carId = carResult.data.id

                    // Upload photo if provided
                    if (photoUri != null) {
                        val file = uriToFile(photoUri, context)
                        if (file != null) {
                            carRepository.addPhoto(carId, "Auto foto", file)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = carResult.message ?: "Fout bij toevoegen auto"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, geocodingError = null)
    }

    private fun uriToFile(uri: Uri, context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
