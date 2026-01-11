package com.example.rentmycar_android_app.network

import com.example.rentmycar_android_app.model.DrivingBehavior
import com.example.rentmycar_android_app.model.DrivingDataRequest
import com.example.rentmycar_android_app.model.DrivingStatsResponse
import com.example.rentmycar_android_app.model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model.UserBonus
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @PATCH("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest  // <- Gebruik UpdateProfileRequest in plaats van User
    ): Response<SimpleResponse>

    @GET("bonus")
    suspend fun getBonusPoints(@Header("Authorization") token: String): Response<UserBonus>

    @POST("driving-behavior")
    suspend fun saveDrivingBehavior(
        @Header("Authorization") token: String,
        @Body data: DrivingDataRequest
    ): Response<DrivingBehavior>

    @GET("driving-behavior")
    suspend fun getDrivingStats(
        @Header("Authorization") token: String
    ): Response<DrivingStatsResponse>
}