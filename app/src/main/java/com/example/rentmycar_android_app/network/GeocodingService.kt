package com.example.rentmycar_android_app.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("/reverse")
    fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): Call<GeocodingResponse>

    @GET("search")
    suspend fun forwardGeocode(
        @Query("street") street: String,
        @Query("postalcode") postalcode: String,
        @Query("country") country: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<GeocodingResponse>
}
