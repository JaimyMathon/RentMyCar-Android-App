package com.example.rentmycar_android_app.network

import com.example.rentmycar_android_app.model.DrivingDataRequest
import com.example.rentmycar_android_app.model.DrivingStatsResponse
import com.example.rentmycar_android_app.model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.UserBonus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

// Response model for saving driving behavior
data class DrivingBehaviorResponse(
    val isSuccess: Boolean = false,
    val message: String? = null,
    val points: Int = 0,
    val rating: String? = null
)

interface ApiService {

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @PATCH("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<SimpleResponseDto>

    @GET("bonus-points")
    suspend fun getBonusPoints(
        @Header("Authorization") token: String
    ): Response<UserBonus>

    @GET("driving-stats")
    suspend fun getDrivingStats(
        @Header("Authorization") token: String
    ): Response<DrivingStatsResponse>

    @POST("driving-behavior")
    suspend fun saveDrivingBehavior(
        @Header("Authorization") token: String,
        @Body request: DrivingDataRequest
    ): Response<DrivingBehaviorResponse>
}
