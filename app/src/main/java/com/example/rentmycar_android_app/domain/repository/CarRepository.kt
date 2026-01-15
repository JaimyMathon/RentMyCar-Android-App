package com.example.rentmycar_android_app.domain.repository

import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.util.Result
import java.io.File

interface CarRepository {
    suspend fun getCars(): Result<List<CarDto>>
    suspend fun getCarById(id: String): Result<CarDto>
    suspend fun getCarsByOwner(ownerId: String): Result<List<CarDto>>
    suspend fun addCar(request: AddCarRequest): Result<CarDto>
    suspend fun addPhoto(carId: String, description: String, file: File): Result<PhotoDto>
    suspend fun updateCar(id: String, request: AddCarRequest): Result<CarDto>
    suspend fun deleteCar(id: String): Result<Boolean>
    suspend fun geocodeAddress(street: String, postcode: String, country: String): Result<Pair<Double, Double>>

    suspend fun getCarPhotos(carId: String): Result<List<PhotoDto>>
}
