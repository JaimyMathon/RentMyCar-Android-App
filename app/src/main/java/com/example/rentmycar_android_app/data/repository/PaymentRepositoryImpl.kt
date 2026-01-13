package com.example.rentmycar_android_app.data.repository

import com.example.rentmycar_android_app.data.auth.TokenManager
import com.example.rentmycar_android_app.domain.repository.PaymentRepository
import com.example.rentmycar_android_app.network.PaymentDto
import com.example.rentmycar_android_app.network.PaymentResponseDto
import com.example.rentmycar_android_app.network.PaymentService
import com.example.rentmycar_android_app.network.ProcessPaymentRequest
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentService: PaymentService,
    private val tokenManager: TokenManager
) : PaymentRepository {

    override suspend fun getPayment(id: String): Result<List<PaymentDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.Error(
                        IllegalStateException("No token"),
                        "Geen token gevonden"
                    )
                }

                val response = paymentService.getPayment("Bearer $token", id)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Fout bij ophalen betaling"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }

    override suspend fun processPayment(request: ProcessPaymentRequest): Result<PaymentResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    return@withContext Result.Error(
                        IllegalStateException("No token"),
                        "Geen token gevonden"
                    )
                }

                val response = paymentService.processPayment("Bearer $token", request)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error(
                        HttpException(response),
                        "Fout bij verwerken betaling"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Netwerkfout: ${e.message}")
            }
        }
    }
}
