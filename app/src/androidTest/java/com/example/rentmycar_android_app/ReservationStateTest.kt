package com.example.rentmycar_android_app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests voor ReservationState.isFormValid
 *
 * Doel:
 * - Controleren dat het formulier alleen geldig is wanneer alle verplichte velden zijn ingevuld.
 */
class ReservationStateTest {

    @Test
    fun isFormValid_returnsTrue_whenAllFieldsFilled() {
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "05-01-2024",
            kilometers = "100"
        )

        val result = state.isFormValid

        assertTrue("Form moet geldig zijn wanneer alle velden zijn ingevuld", result)
    }

    @Test
    fun isFormValid_returnsFalse_whenFromDateIsEmpty() {
        val state = ReservationState(
            fromDate = "",
            toDate = "05-01-2024",
            kilometers = "100"
        )

        val result = state.isFormValid

        assertFalse("Form mag niet geldig zijn zonder fromDate", result)
    }

    @Test
    fun isFormValid_returnsFalse_whenToDateIsEmpty() {
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "",
            kilometers = "100"
        )

        val result = state.isFormValid

        assertFalse("Form mag niet geldig zijn zonder toDate", result)
    }

    @Test
    fun isFormValid_returnsFalse_whenKilometersIsEmpty() {
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "05-01-2024",
            kilometers = ""
        )

        val result = state.isFormValid

        assertFalse("Form mag niet geldig zijn zonder kilometers", result)
    }
}