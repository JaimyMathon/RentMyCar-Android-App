package com.example.rentmycar_android_app.domain.repository

import com.example.rentmycar_android_app.model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model.UserBonus
import com.example.rentmycar_android_app.network.SimpleResponse
import com.example.rentmycar_android_app.util.Result

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<SimpleResponse>
    suspend fun getBonusPoints(): Result<UserBonus>
}
