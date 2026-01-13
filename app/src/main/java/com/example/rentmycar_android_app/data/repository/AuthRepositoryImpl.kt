package com.example.rentmycar_android_app.data.repository

import com.example.rentmycar_android_app.data.auth.TokenManager
import com.example.rentmycar_android_app.domain.repository.AuthRepository
import com.example.rentmycar_android_app.network.*
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = authService.login(request).execute()

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUsername(authResponse.username)
                    Result.Success(authResponse)
                } else {
                    Result.Error(
                        HttpException(response),
                        "E-mail of wachtwoord onjuist"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authService.register(request).execute()

                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Registratie mislukt"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<SimpleResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authService.resetPassword(request).execute()

                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Reset wachtwoord mislukt"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }

    override suspend fun logout() {
        tokenManager.clearAll()
    }
}
