package com.example.rentmycar_android_app.data.repository

import com.example.rentmycar_android_app.data.auth.TokenManager
import com.example.rentmycar_android_app.domain.repository.UserRepository
import com.example.rentmycar_android_app.model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model.UserBonus
import com.example.rentmycar_android_app.network.ApiService
import com.example.rentmycar_android_app.network.SimpleResponse
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : UserRepository {

    override suspend fun getProfile(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.Error(
                        IllegalStateException("No token"),
                        "Geen token gevonden"
                    )
                }

                val response = apiService.getProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Fout bij ophalen profiel"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<SimpleResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.Error(
                        IllegalStateException("No token"),
                        "Geen token gevonden"
                    )
                }

                val response = apiService.updateProfile("Bearer $token", request)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Fout bij opslaan profiel"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }

    override suspend fun getBonusPoints(): Result<UserBonus> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.Error(
                        IllegalStateException("No token"),
                        "Geen token gevonden"
                    )
                }

                val response = apiService.getBonusPoints("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Fout bij ophalen bonuspunten"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }
}
