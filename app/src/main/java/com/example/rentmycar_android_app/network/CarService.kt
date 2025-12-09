package com.example.rentmycar_android_app.network

import retrofit2.http.GET

interface CarService {

    // volledige pad vanaf baseUrl
    @GET("get-cars")
    suspend fun getCars(): CarResponse
}