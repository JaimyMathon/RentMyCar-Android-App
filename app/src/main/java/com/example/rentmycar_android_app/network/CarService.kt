package com.example.rentmycar_android_app.network

import retrofit2.http.GET

interface CarService {

    // volledige pad vanaf baseUrl
    @GET("get-cars")
    suspend fun getCars(): CarResponse
}
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CarService {
    @GET("/get-car/{id}")
    fun getCar(@Path("id") id: String): Call<CarResponse>
}
