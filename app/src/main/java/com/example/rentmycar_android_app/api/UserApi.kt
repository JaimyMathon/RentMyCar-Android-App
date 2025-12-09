// Bestandsnaam: app/src/main/java/com/example/rentmycar_android_app/api/ApiService.kt

package com.example.rentmycar_android_app.api

import com.example.rentmycar_android_app.data.ResetPasswordWithTokenRequest
import com.example.rentmycar_android_app.network.SimpleResponse
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ApiService {
    @PATCH("/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordWithTokenRequest): SimpleResponse
}