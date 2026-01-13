package com.example.rentmycar_android_app.data.auth

import kotlinx.coroutines.flow.Flow

interface TokenManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    fun getTokenFlow(): Flow<String?>
    suspend fun saveUsername(username: String)
    suspend fun getUsername(): String?
    suspend fun clearAll()
}
