package com.example.rentmycar_android_app.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RoutingService {
    @GET("/route/v1/driving/{coordinates}")
    fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full"
    ): Call<RouteResponse>
}
