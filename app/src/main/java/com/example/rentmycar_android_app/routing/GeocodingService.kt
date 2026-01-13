package com.example.rentmycar_android_app.routing

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
}