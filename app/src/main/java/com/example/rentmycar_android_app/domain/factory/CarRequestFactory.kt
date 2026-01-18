package com.example.rentmycar_android_app.domain.factory

import com.example.rentmycar_android_app.network.AddCarRequest

object CarRequestFactory {

    sealed class ValidationResult {
        data class Valid(val request: AddCarRequest) : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }

    fun create(
        ownerId: String,
        brand: String,
        model: String,
        licensePlate: String,
        category: String,
        pricePerTimeSlot: Double,
        latitude: Double,
        longitude: Double,
        costPerKm: Double,
        fuelCost: Double,
        maintenance: Double,
        insurance: Double,
        depreciation: Double
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (ownerId.isBlank()) {
            errors.add("Gebruiker ID is verplicht")
        }
        if (brand.isBlank()) {
            errors.add("Merk is verplicht")
        }
        if (model.isBlank()) {
            errors.add("Model is verplicht")
        }
        if (licensePlate.isBlank()) {
            errors.add("Kenteken is verplicht")
        }
        if (category.isBlank()) {
            errors.add("Categorie is verplicht")
        }
        if (pricePerTimeSlot < 0) {
            errors.add("Prijs per dag moet positief zijn")
        }

        if (errors.isNotEmpty()) {
            return ValidationResult.Invalid(errors)
        }

        return ValidationResult.Valid(
            AddCarRequest(
                ownerId = ownerId,
                brand = brand,
                model = model,
                licensePlate = licensePlate,
                category = category,
                pricePerTimeSlot = pricePerTimeSlot,
                latitude = latitude,
                longitude = longitude,
                costPerKm = costPerKm,
                fuelCost = fuelCost,
                maintenance = maintenance,
                insurance = insurance,
                depreciation = depreciation
            )
        )
    }
}