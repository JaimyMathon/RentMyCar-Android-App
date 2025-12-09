package com.example.rentmycar_android_app.network

import com.google.gson.JsonObject

data class RouteResponse(
    val routes: List<Route>,
    val waypoints: List<Waypoint>?
)

data class Route(
    val geometry: JsonObject,
    val legs: List<RouteLeg>,
    val distance: Double,
    val duration: Double
)

data class RouteLeg(
    val distance: Double,
    val duration: Double
)

data class Waypoint(
    val hint: String?,
    val distance: Double?,
    val name: String?,
    val location: List<Double>?
)
