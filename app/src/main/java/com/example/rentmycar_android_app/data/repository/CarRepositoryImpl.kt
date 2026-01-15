package com.example.rentmycar_android_app.data.repository

import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService
import com.example.rentmycar_android_app.network.GeocodingService
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val carService: CarService,
    private val geocodingService: GeocodingService
) : CarRepository {

    override suspend fun getCars(): Result<List<CarDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = carService.getCars()
                Result.Success(response.cars)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij ophalen auto's: ${e.message}")
            }
        }
    }

    override suspend fun getCarById(id: String): Result<CarDto> {
        return withContext(Dispatchers.IO) {
            try {
                val car = carService.getCarById(id)
                Result.Success(car)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij ophalen auto: ${e.message}")
            }
        }
    }

    override suspend fun getCarsByOwner(ownerId: String): Result<List<CarDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = carService.getCarsByOwner(ownerId)
                Result.Success(response.cars)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij ophalen auto's: ${e.message}")
            }
        }
    }

    override suspend fun addCar(request: AddCarRequest): Result<CarDto> {
        return withContext(Dispatchers.IO) {
            try {
                val car = carService.addCar(request)
                Result.Success(car)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij toevoegen auto: ${e.message}")
            }
        }
    }

    override suspend fun addPhoto(carId: String, description: String, file: File): Result<PhotoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val carIdBody = carId.toRequestBody("text/plain".toMediaTypeOrNull())
                val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("url", file.name, requestFile)

                val photo = carService.addPhoto(carIdBody, descBody, filePart)
                Result.Success(photo)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij uploaden foto: ${e.message}")
            }
        }
    }

    override suspend fun updateCar(id: String, request: AddCarRequest): Result<CarDto> {
        return withContext(Dispatchers.IO) {
            try {
                val car = carService.updateCar(id, request)
                Result.Success(car)
            } catch (e: Exception) {
                Result.Error(e, "Fout bij bijwerken auto: ${e.message}")
            }
        }
    }

    override suspend fun deleteCar(id: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = carService.deleteCar(id)
                if (response.isSuccessful) {
                    Result.Success(true)
                } else {
                    Result.Error(Exception("Delete failed"), "Kan auto niet verwijderen")
                }
            } catch (e: Exception) {
                Result.Error(e, "Fout bij verwijderen auto: ${e.message}")
            }
        }
    }

    override suspend fun geocodeAddress(street: String, postcode: String, country: String): Result<Pair<Double, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = geocodingService.forwardGeocode(street, postcode, country)
                if (results.isNotEmpty()) {
                    val first = results.first()
                    val lat = first.lat ?: throw Exception("Geen latitude gevonden")
                    val lon = first.lon ?: throw Exception("Geen longitude gevonden")
                    Result.Success(Pair(lat, lon))
                } else {
                    Result.Error(Exception("No results"), "Adres niet gevonden")
                }
            } catch (e: Exception) {
                Result.Error(e, "Fout bij geocoderen: ${e.message}")
            }
        }
    }
}
