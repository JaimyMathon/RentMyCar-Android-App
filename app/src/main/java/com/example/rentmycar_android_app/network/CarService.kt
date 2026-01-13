package com.example.rentmycar_android_app.network

import retrofit2.http.GET
import retrofit2.http.Path

interface CarService {

    @GET("get-cars")
    suspend fun getCars(): CarResponse  // <-- dit moet matchen met carPage JSON

    @GET("get-car/{id}")
    suspend fun getCarById(@Path("id") id: String): CarDto

    @GET("get-photo/{carId}")
    suspend fun getCarPhotos(@Path("carId") carId: String): List<PhotoDto>
}