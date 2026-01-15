package com.example.rentmycar_android_app.network

import com.google.gson.annotations.SerializedName

data class CarListResponse(
    @SerializedName("cars") val cars: List<CarDto> = emptyList()
)

data class CarDto(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val id: String? = null,

    @SerializedName("brand") val brand: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("licensePlate") val licensePlate: String? = null,
    @SerializedName("category") val category: String? = null,

    @SerializedName("pricePerTimeSlot") val pricePerTimeSlot: Double? = null,
    @SerializedName("costPerKm") val costPerKm: Double? = null,

    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,

    @SerializedName("tco") val tco: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("added_by") val addedBy: String? = null,

    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("image") val image: String? = null  // Fallback veldnaam
) {
    val safeId: String get() = (mongoId ?: id ?: "").trim()
    val carImageUrl: String? get() = imageUrl ?: image
}

data class SimpleResponseDto(
    @SerializedName("isSuccess") val isSuccess: Boolean = false,
    @SerializedName("message") val message: String? = null
)