package com.example.rentmycar_android_app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.domain.repository.ReservationRepository
import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CreateReservationRequest
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.UpdateReservationRequest
import com.example.rentmycar_android_app.util.Result
import com.example.rentmycar_android_app.viewmodels.ReservationsViewModel
import java.io.File
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.ZonedDateTime

class ReservationsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeReservationRepository: FakeReservationRepositoryForUI
    private lateinit var fakeCarRepository: FakeCarRepositoryForReservationsUI
    private lateinit var viewModel: ReservationsViewModel
    private var backClicked = false
    private var navigatedToLocation: Pair<Double, Double>? = null

    @Before
    fun setup() {
        fakeReservationRepository = FakeReservationRepositoryForUI()
        fakeCarRepository = FakeCarRepositoryForReservationsUI()
        backClicked = false
        navigatedToLocation = null
    }

    private fun createViewModel(): ReservationsViewModel {
        return ReservationsViewModel(fakeReservationRepository, fakeCarRepository)
    }

    @Test
    fun reservationsScreen_displaysTitle() {
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Mijn reserveringen").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_displaysTabs() {
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Komend").assertIsDisplayed()
        composeTestRule.onNodeWithText("Afgehandeld").assertIsDisplayed()
        composeTestRule.onNodeWithText("cancelled").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_showsLoadingIndicator_whenLoading() {
        fakeReservationRepository.setDelayResponse(true)
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.onNode(hasTestTag("loading") or hasContentDescription("loading"))
            .assertExists()
    }

    @Test
    fun reservationsScreen_showsEmptyMessage_whenNoReservations() {
        fakeReservationRepository.setReservations(emptyList())
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Geen reserveringen gevonden").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_showsErrorMessage_whenError() {
        fakeReservationRepository.setShouldReturnError(true, "Network error")
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_displaysReservationCard_withCarDetails() {
        val car = CarDto(
            id = "car1",
            brand = "Tesla",
            model = "Model 3",
            pricePerTimeSlot = 75.0,
            category = "BEV"
        )
        val reservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = car,
            status = "pending",
            startTime = ZonedDateTime.now().plusDays(1).toString(),
            endTime = ZonedDateTime.now().plusDays(2).toString()
        )
        fakeReservationRepository.setReservations(listOf(reservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Tesla Model 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("€75/dag").assertIsDisplayed()
        composeTestRule.onNodeWithText("BEV").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_showsCancelButton_forUpcomingReservations() {
        val reservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = CarDto(id = "car1", brand = "Tesla", model = "Model 3"),
            status = "pending",
            startTime = ZonedDateTime.now().plusDays(1).toString(),
            endTime = ZonedDateTime.now().plusDays(2).toString()
        )
        fakeReservationRepository.setReservations(listOf(reservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Annuleren").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_hidesCancel_forStartedReservations() {
        val reservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = CarDto(id = "car1", brand = "Tesla", model = "Model 3"),
            status = "pending",
            startTime = ZonedDateTime.now().minusDays(1).toString(),
            endTime = ZonedDateTime.now().plusDays(1).toString()
        )
        fakeReservationRepository.setReservations(listOf(reservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Annuleren").assertDoesNotExist()
    }

    @Test
    fun reservationsScreen_switchToCompletedTab_showsCompletedReservations() {
        val pendingReservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = CarDto(id = "car1", brand = "Tesla", model = "Model 3"),
            status = "pending"
        )
        val completedReservation = ReservationDto(
            id = "res2",
            carId = "car2",
            car = CarDto(id = "car2", brand = "BMW", model = "i4"),
            status = "completed"
        )
        fakeReservationRepository.setReservations(listOf(pendingReservation, completedReservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Tesla Model 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("BMW i4").assertDoesNotExist()

        composeTestRule.onNodeWithText("Afgehandeld").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("BMW i4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tesla Model 3").assertDoesNotExist()
    }

    @Test
    fun reservationsScreen_switchToCancelledTab_showsCancelledReservations() {
        val pendingReservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = CarDto(id = "car1", brand = "Tesla", model = "Model 3"),
            status = "pending"
        )
        val cancelledReservation = ReservationDto(
            id = "res3",
            carId = "car3",
            car = CarDto(id = "car3", brand = "Audi", model = "e-tron"),
            status = "cancelled"
        )
        fakeReservationRepository.setReservations(listOf(pendingReservation, cancelledReservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("cancelled").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Audi e-tron").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tesla Model 3").assertDoesNotExist()
    }

    @Test
    fun reservationsScreen_backButton_triggersNavigation() {
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Terug").performClick()

        assertTrue(backClicked)
    }

    @Test
    fun reservationsScreen_navigateButton_triggersLocationNavigation() {
        val car = CarDto(
            id = "car1",
            brand = "Tesla",
            model = "Model 3",
            latitude = 52.3676,
            longitude = 4.9041
        )
        val reservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = car,
            status = "pending",
            startTime = ZonedDateTime.now().plusDays(1).toString()
        )
        fakeReservationRepository.setReservations(listOf(reservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Navigate").performClick()

        assertNotNull(navigatedToLocation)
        assertEquals(52.3676, navigatedToLocation!!.first, 0.001)
        assertEquals(4.9041, navigatedToLocation!!.second, 0.001)
    }

    @Test
    fun reservationsScreen_cancelButton_cancelsReservation() {
        val reservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = CarDto(id = "car1", brand = "Tesla", model = "Model 3"),
            status = "pending",
            startTime = ZonedDateTime.now().plusDays(1).toString()
        )
        fakeReservationRepository.setReservations(listOf(reservation))
        fakeReservationRepository.setReservationsAfterCancel(emptyList())
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        // Click cancel button
        composeTestRule.onNodeWithText("Annuleren").performClick()
        composeTestRule.waitForIdle()

        // After cancellation, the list should be empty
        composeTestRule.onNodeWithText("Geen reserveringen gevonden").assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_displaysDateTimeCorrectly() {
        val startTime = "2024-12-25T10:00:00+01:00"
        val endTime = "2024-12-26T18:00:00+01:00"
        val reservation = ReservationDto(
            id = "res1",
            carId = "car1",
            car = CarDto(id = "car1", brand = "Tesla", model = "Model 3"),
            status = "confirmed",
            startTime = startTime,
            endTime = endTime
        )
        fakeReservationRepository.setReservations(listOf(reservation))
        viewModel = createViewModel()

        composeTestRule.setContent {
            ReservationsScreen(
                onBackClick = { backClicked = true },
                onNavigateToLocation = { lat, lon -> navigatedToLocation = Pair(lat, lon) },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        // Verify date labels are present
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eind").assertIsDisplayed()

        // Verify formatted dates are displayed
        composeTestRule.onNodeWithText("25-12-2024 10:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("26-12-2024 18:00").assertIsDisplayed()
    }
}

/**
 * Fake implementation of ReservationRepository for UI testing
 */
class FakeReservationRepositoryForUI : ReservationRepository {

    private var reservations: List<ReservationDto> = emptyList()
    private var reservationsAfterCancel: List<ReservationDto>? = null
    private var shouldReturnError = false
    private var errorMessage: String? = null
    private var delayResponse = false

    fun setReservations(reservations: List<ReservationDto>) {
        this.reservations = reservations
    }

    fun setReservationsAfterCancel(reservations: List<ReservationDto>) {
        this.reservationsAfterCancel = reservations
    }

    fun setShouldReturnError(shouldError: Boolean, message: String? = null) {
        this.shouldReturnError = shouldError
        this.errorMessage = message
    }

    fun setDelayResponse(delay: Boolean) {
        this.delayResponse = delay
    }

    override suspend fun getReservations(): Result<List<ReservationDto>> {
        if (delayResponse) {
            kotlinx.coroutines.delay(5000)
        }
        return if (shouldReturnError) {
            Result.Error(Exception("Test error"), errorMessage)
        } else {
            Result.Success(reservations)
        }
    }

    override suspend fun createReservation(request: CreateReservationRequest): Result<ReservationDto> {
        return Result.Success(
            ReservationDto(
                id = "new-id",
                carId = request.carId,
                renterId = request.renterId,
                startTime = request.startTime,
                endTime = request.endTime,
                estimatedDistance = request.estimatedDistance,
                status = "pending"
            )
        )
    }

    override suspend fun updateReservation(id: String, request: UpdateReservationRequest): Result<ReservationDto> {
        val existing = reservations.find { it.id == id }
        return if (existing != null) {
            Result.Success(existing)
        } else {
            Result.Error(Exception("Reservation not found"))
        }
    }

    override suspend fun cancelReservation(id: String): Result<ReservationDto> {
        val existing = reservations.find { it.id == id }
        return if (existing != null) {
            reservationsAfterCancel?.let { reservations = it }
            Result.Success(existing.copy(status = "cancelled"))
        } else {
            Result.Error(Exception("Reservation not found"))
        }
    }
}

/**
 * Fake implementation of CarRepository for Reservations UI testing
 */
class FakeCarRepositoryForReservationsUI : CarRepository {

    private var cars: List<CarDto> = emptyList()
    private var photosByCarId: MutableMap<String, List<PhotoDto>> = mutableMapOf()

    fun setCars(cars: List<CarDto>) {
        this.cars = cars
    }

    fun setPhotos(carId: String, photos: List<PhotoDto>) {
        photosByCarId[carId] = photos
    }

    override suspend fun getCars(): Result<List<CarDto>> {
        return Result.Success(cars)
    }

    override suspend fun getCarById(id: String): Result<CarDto> {
        val car = cars.find { it.id == id }
        return if (car != null) {
            Result.Success(car)
        } else {
            Result.Error(Exception("Car not found"))
        }
    }

    override suspend fun getCarPhotos(carId: String): Result<List<PhotoDto>> {
        return Result.Success(photosByCarId[carId] ?: emptyList())
    }

    override suspend fun getCarsByOwner(ownerId: String): Result<List<CarDto>> {
        return Result.Success(cars.filter { it.ownerId == ownerId })
    }

    override suspend fun addCar(request: AddCarRequest): Result<CarDto> {
        return Result.Success(CarDto(id = "new-car", brand = request.brand, model = request.model))
    }

    override suspend fun addPhoto(carId: String, description: String, file: File): Result<PhotoDto> {
        return Result.Success(PhotoDto(url = "/photos/$carId/photo.jpg", carId = carId))
    }

    override suspend fun updateCar(id: String, request: AddCarRequest): Result<CarDto> {
        return Result.Success(CarDto(id = id, brand = request.brand, model = request.model))
    }

    override suspend fun deleteCar(id: String): Result<Boolean> {
        return Result.Success(true)
    }

    override suspend fun geocodeAddress(street: String, postcode: String, country: String): Result<Pair<Double, Double>> {
        return Result.Success(Pair(52.0, 4.0))
    }
}