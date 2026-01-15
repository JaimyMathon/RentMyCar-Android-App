package com.example.rentmycar_android_app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CarUploadService {

    @Multipart
    @POST("add-car")
    suspend fun addCar(
        @Part("brand") brand: RequestBody,
        @Part("model") model: RequestBody,
        @Part("licensePlate") licensePlate: RequestBody,
        @Part("category") category: RequestBody,
        @Part("pricePerTimeSlot") pricePerTimeSlot: RequestBody,
        @Part("costPerKm") costPerKm: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part image: MultipartBody.Part
    ): SimpleResponseDto
}