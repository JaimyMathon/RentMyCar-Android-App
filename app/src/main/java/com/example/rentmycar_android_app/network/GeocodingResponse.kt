package com.example.rentmycar_android_app.network

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    val address: Address? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val display_name: String? = null
)

data class Address(
    val house_number: String? = null,
    val road: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null,
    @SerializedName("town")
    val town: String? = null,
    @SerializedName("village")
    val village: String? = null
) {
    fun toFullAddress(): String {
        val parts = mutableListOf<String>()

        if (!road.isNullOrEmpty()) parts.add(road)
        if (!house_number.isNullOrEmpty()) parts.add(house_number)

        val cityName = city ?: town ?: village ?: suburb
        if (!cityName.isNullOrEmpty()) parts.add(cityName)

        if (!postcode.isNullOrEmpty()) parts.add(postcode)
        if (!country.isNullOrEmpty()) parts.add(country)

        return if (parts.isNotEmpty()) parts.joinToString(", ") else ""
    }
}
