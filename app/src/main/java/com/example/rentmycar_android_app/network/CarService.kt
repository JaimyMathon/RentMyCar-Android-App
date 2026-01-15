package com.example.rentmycar_android_app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CarService {

    @GET("get-cars")
    suspend fun getCars(): CarResponse

    @GET("get-cars")
    suspend fun getCarsByOwner(@Query("ownerId") ownerId: String): CarResponse

    @GET("get-car/{id}")
    suspend fun getCarById(@Path("id") id: String): CarDto

    @GET("get-photo/{carId}")
    suspend fun getCarPhotos(@Path("carId") carId: String): List<PhotoDto>

    @POST("add-car")
    suspend fun addCar(@Body request: AddCarRequest): CarDto

    @Multipart
    @POST("add-photo")
    suspend fun addPhoto(
        @Part("carId") carId: RequestBody,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): PhotoDto

    @PATCH("update-car/{id}")
    suspend fun updateCar(
        @Path("id") id: String,
        @Body request: AddCarRequest
    ): CarDto

    @DELETE("delete-car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>
}