package com.example.rentmycar_android_app.data.repository

import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val carService: CarService
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
}
