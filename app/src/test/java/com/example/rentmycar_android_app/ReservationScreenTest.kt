package com.example.rentmycar_android_app

import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CarService
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.ReservationService
import com.example.rentmycar_android_app.ui.DateFormatter
import com.example.rentmycar_android_app.ui.LoadCarDataUseCase
import com.example.rentmycar_android_app.ui.ReservationState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit Tests voor ReservationScreen componenten
 */
class ReservationScreenTest {

    @Mock
    private lateinit var carService: CarService

    @Mock
    private lateinit var reservationService: ReservationService

    private lateinit var loadCarDataUseCase: LoadCarDataUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        loadCarDataUseCase = LoadCarDataUseCase(carService, reservationService)
    }

    // =========================================================================
    // 1. RESERVATIONSTATE TESTS - Form Validatie
    // =========================================================================

    @Test
    fun `test ReservationState isFormValid - alle velden ingevuld - returns true`() {
        // Arrange
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "05-01-2024",
            kilometers = "100"
        )

        // Act
        val isValid = state.isFormValid

        // Assert
        assertTrue("Form moet valid zijn met alle velden ingevuld", isValid)
    }

    @Test
    fun `test ReservationState isFormValid - fromDate leeg - returns false`() {
        // Arrange
        val state = ReservationState(
            fromDate = "",
            toDate = "05-01-2024",
            kilometers = "100"
        )

        // Act
        val isValid = state.isFormValid

        // Assert
        assertFalse("Form moet invalid zijn zonder fromDate", isValid)
    }

    @Test
    fun `test ReservationState isFormValid - toDate leeg - returns false`() {
        // Arrange
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "",
            kilometers = "100"
        )

        // Act
        val isValid = state.isFormValid

        // Assert
        assertFalse("Form moet invalid zijn zonder toDate", isValid)
    }

    @Test
    fun `test ReservationState isFormValid - kilometers leeg - returns false`() {
        // Arrange
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "05-01-2024",
            kilometers = ""
        )

        // Act
        val isValid = state.isFormValid

        // Assert
        assertFalse("Form moet invalid zijn zonder kilometers", isValid)
    }

    @Test
    fun `test ReservationState isFormValid - alle velden leeg - returns false`() {
        // Arrange
        val state = ReservationState()

        // Act
        val isValid = state.isFormValid

        // Assert
        assertFalse("Form moet invalid zijn met lege state", isValid)
    }

    // =========================================================================
    // 2. DATEFORMATTER TESTS - Datum parsing
    // =========================================================================

    @Test
    fun `test DateFormatter formatReservationDate - valid ISO date - returns formatted`() {
        // Arrange
        val isoDate = "2024-01-15T10:30:00+01:00"

        // Act
        val result = DateFormatter.formatReservationDate(isoDate)

        // Assert
        assertEquals("Datum moet correct geformat worden", "15-01-2024", result)
    }

    @Test
    fun `test DateFormatter formatReservationDate - null date - returns empty string`() {
        // Arrange
        val isoDate: String? = null

        // Act
        val result = DateFormatter.formatReservationDate(isoDate)

        // Assert
        assertEquals("Null datum moet lege string teruggeven", "", result)
    }

    @Test
    fun `test DateFormatter formatReservationDate - invalid date - returns original`() {
        // Arrange - Edge case: ongeldige datum
        val invalidDate = "dit-is-geen-datum"

        // Act
        val result = DateFormatter.formatReservationDate(invalidDate)

        // Assert
        assertEquals("Ongeldige datum moet origineel teruggeven", invalidDate, result)
    }

    // =========================================================================
    // 3. LOADCARDATAUSECASE TESTS - Business Logic
    // =========================================================================

    @Test
    fun `test LoadCarDataUseCase execute - successful car fetch - returns car name`() = runTest {
        // Arrange
        val carId = "car123"
        val mockCar = mock(CarDto::class.java).apply {
            `when`(this.brand).thenReturn("Tesla")
            `when`(this.model).thenReturn("Model 3")
        }
        `when`(carService.getCarById(carId)).thenReturn(mockCar)
        `when`(reservationService.getReservationsByCarId(carId)).thenReturn(emptyList())

        // Act
        val result = loadCarDataUseCase.execute(carId)

        // Assert
        assertTrue("Result moet success zijn", result.isSuccess)
        assertEquals("Tesla Model 3", result.getOrNull()?.first)
    }

    @Test
    fun `test LoadCarDataUseCase execute - car service throws exception - returns null car name`() = runTest {
        // Arrange
        val carId = "car123"
        `when`(carService.getCarById(carId)).thenThrow(RuntimeException("Network error"))
        `when`(reservationService.getReservationsByCarId(carId)).thenReturn(emptyList())

        // Act
        val result = loadCarDataUseCase.execute(carId)

        // Assert
        assertTrue("Result moet success zijn (graceful degradation)", result.isSuccess)
        assertNull("Car name moet null zijn bij error", result.getOrNull()?.first)
    }

    @Test
    fun `test LoadCarDataUseCase execute - filter only pending and confirmed reservations`() = runTest {
        // Arrange
        val carId = "car123"

        // Mock ReservationDto objecten - zonder named parameters
        val reservation1 = mock(ReservationDto::class.java).apply {
            `when`(this.id).thenReturn("1")
            `when`(this.status).thenReturn("pending")
        }
        val reservation2 = mock(ReservationDto::class.java).apply {
            `when`(this.id).thenReturn("2")
            `when`(this.status).thenReturn("confirmed")
        }
        val reservation3 = mock(ReservationDto::class.java).apply {
            `when`(this.id).thenReturn("3")
            `when`(this.status).thenReturn("cancelled")
        }
        val reservation4 = mock(ReservationDto::class.java).apply {
            `when`(this.id).thenReturn("4")
            `when`(this.status).thenReturn("completed")
        }

        val allReservations = listOf(reservation1, reservation2, reservation3, reservation4)

        `when`(carService.getCarById(carId)).thenThrow(RuntimeException())
        `when`(reservationService.getReservationsByCarId(carId)).thenReturn(allReservations)

        // Act
        val result = loadCarDataUseCase.execute(carId)

        // Assert
        val filteredReservations = result.getOrNull()?.second
        assertEquals("Alleen pending en confirmed reserveringen", 2, filteredReservations?.size)
        assertTrue(
            "Moet pending bevatten",
            filteredReservations?.any { it.status == "pending" } == true
        )
        assertTrue(
            "Moet confirmed bevatten",
            filteredReservations?.any { it.status == "confirmed" } == true
        )
        assertFalse(
            "Mag geen cancelled bevatten",
            filteredReservations?.any { it.status == "cancelled" } == true
        )
    }

    @Test
    fun `test LoadCarDataUseCase execute - empty reservations list - returns empty list`() = runTest {
        // Arrange
        val carId = "car123"
        `when`(carService.getCarById(carId)).thenThrow(RuntimeException())
        `when`(reservationService.getReservationsByCarId(carId)).thenReturn(emptyList())

        // Act
        val result = loadCarDataUseCase.execute(carId)

        // Assert
        assertTrue("Result moet success zijn", result.isSuccess)
        assertTrue("Reservations moet leeg zijn", result.getOrNull()?.second?.isEmpty() == true)
    }

    @Test
    fun `test LoadCarDataUseCase execute - reservation service throws exception - returns empty list`() = runTest {
        // Arrange
        val carId = "car123"
        `when`(carService.getCarById(carId)).thenThrow(RuntimeException())
        `when`(reservationService.getReservationsByCarId(carId)).thenThrow(RuntimeException("Network error"))

        // Act
        val result = loadCarDataUseCase.execute(carId)

        // Assert
        assertTrue("Result moet success zijn (graceful degradation)", result.isSuccess)
        assertTrue("Reservations moet leeg zijn bij error", result.getOrNull()?.second?.isEmpty() == true)
    }

    // =========================================================================
    // 4. EXTRA EDGE CASE TESTS
    // =========================================================================

    @Test
    fun `test ReservationState - extreme long kilometer value - stays valid`() {
        // Arrange - Edge case: extreem hoge waarde
        val state = ReservationState(
            fromDate = "01-01-2024",
            toDate = "05-01-2024",
            kilometers = "999999999"
        )

        // Act
        val isValid = state.isFormValid

        // Assert
        assertTrue("Form moet valid blijven met extreme waarde", isValid)
    }

    @Test
    fun `test DateFormatter - date with different timezone - formats correctly`() {
        // Arrange - Edge case: verschillende timezone
        val isoDate = "2024-01-15T10:30:00-05:00" // New York tijd

        // Act
        val result = DateFormatter.formatReservationDate(isoDate)

        // Assert
        assertEquals("Datum moet correct geformat worden ongeacht timezone", "15-01-2024", result)
    }
}