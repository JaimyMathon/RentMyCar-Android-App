package com.example.rentmycar_android_app.domain.validator

object InputValidator {
    const val MIN_PRICE = 0.0
    const val MAX_PRICE_PER_DAY = 1000.0
    const val MAX_PRICE_PER_KM = 10.0

    fun validateAddress(street: String, houseNumber: String, postcode: String): List<String> {
        val errors = mutableListOf<String>()
        if (street.isBlank()) {
            errors.add("Straatnaam is verplicht")
        }
        if (houseNumber.isBlank()) {
            errors.add("Huisnummer is verplicht")
        }
        if (postcode.isBlank()) {
            errors.add("Postcode is verplicht")
        }
        return errors
    }

    fun isValidPricePerDay(price: Double): Boolean {
        return price >= MIN_PRICE && price <= MAX_PRICE_PER_DAY
    }

    fun isValidPricePerKm(price: Double): Boolean {
        return price >= MIN_PRICE && price <= MAX_PRICE_PER_KM
    }
}