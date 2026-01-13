package com.example.rentmycar_android_app.domain.repository

import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.util.Result

interface CarRepository {
    suspend fun getCars(): Result<List<CarDto>>
    suspend fun getCarById(id: String): Result<CarDto>
}
