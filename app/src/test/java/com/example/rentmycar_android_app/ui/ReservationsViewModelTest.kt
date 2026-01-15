package com.example.rentmycar_android_app.ui

import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.domain.repository.ReservationRepository
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.CreateReservationRequest
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.network.ReservationDto
import com.example.rentmycar_android_app.network.UpdateReservationRequest
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationsViewModelTest {

    private lateinit var viewModel: ReservationsViewModel
    private lateinit var fakeReservationRepository: FakeReservationRepository
    private lateinit var fakeCarRepository: FakeCarRepositoryForReservations
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeReservationRepository = FakeReservationRepository()
        fakeCarRepository = FakeCarRepositoryForReservations()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ReservationsViewModel {
        return ReservationsViewModel(fakeReservationRepository, fakeCarRepository)
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = createViewModel()

        // Before advancing, state should be loading
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadReservations success updates state with reservations`() = runTest {
        val reservations = listOf(
            ReservationDto(id = "1", carId = "car1", status = "pending"),
            ReservationDto(id = "2", carId = "car2", status = "confirmed")
        )
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.reservations.size)
    }

    @Test
    fun `loadReservations error updates state with error message`() = runTest {
        fakeReservationRepository.setShouldReturnError(true, "Network error")

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
        assertTrue(state.reservations.isEmpty())
    }

    @Test
    fun `loadReservations fetches car details when car is null but carId exists`() = runTest {
        val car = CarDto(id = "car1", brand = "Tesla", model = "Model 3")
        fakeCarRepository.setCars(listOf(car))

        val reservations = listOf(
            ReservationDto(id = "1", carId = "car1", car = null, status = "pending")
        )
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.reservations.size)
        assertEquals("Tesla", state.reservations[0].car?.brand)
        assertEquals("Model 3", state.reservations[0].car?.model)
    }

    @Test
    fun `loadReservations preserves existing car when already present`() = runTest {
        val embeddedCar = CarDto(id = "car1", brand = "BMW", model = "i4")
        val reservations = listOf(
            ReservationDto(id = "1", carId = "car1", car = embeddedCar, status = "pending")
        )
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("BMW", state.reservations[0].car?.brand)
    }

    @Test
    fun `cancelReservation success reloads reservations`() = runTest {
        val reservations = listOf(
            ReservationDto(id = "1", status = "pending"),
            ReservationDto(id = "2", status = "confirmed")
        )
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.reservations.size)

        // Configure the repository to return updated list after cancel triggers reload
        fakeReservationRepository.setReservationsAfterCancel(listOf(
            ReservationDto(id = "2", status = "confirmed")
        ))

        viewModel.cancelReservation("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.reservations.size)
        assertEquals("2", viewModel.uiState.value.reservations[0].id)
    }

    @Test
    fun `cancelReservation error updates state with error message`() = runTest {
        val reservations = listOf(ReservationDto(id = "1", status = "pending"))
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeReservationRepository.setCancelShouldReturnError(true, "Cancel failed")

        viewModel.cancelReservation("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Cancel failed", viewModel.uiState.value.error)
    }

    @Test
    fun `clearError clears the error message`() = runTest {
        fakeReservationRepository.setShouldReturnError(true, "Some error")

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Some error", viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `getCarPhotoUrl returns photo URL when photos exist`() = runTest {
        fakeCarRepository.setPhotos("car1", listOf(
            PhotoDto(url = "/photos/car1/photo1.jpg", carId = "car1")
        ))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val photoUrl = viewModel.getCarPhotoUrl("car1")

        assertEquals("http://10.0.2.2:8080/photos/car1/photo1.jpg", photoUrl)
    }

    @Test
    fun `getCarPhotoUrl returns null when no photos exist`() = runTest {
        fakeCarRepository.setPhotos("car1", emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val photoUrl = viewModel.getCarPhotoUrl("car1")

        assertNull(photoUrl)
    }

    @Test
    fun `getCarPhotoUrl returns null on error`() = runTest {
        fakeCarRepository.setPhotosShouldReturnError(true)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val photoUrl = viewModel.getCarPhotoUrl("car1")

        assertNull(photoUrl)
    }

    @Test
    fun `reservations with different statuses are loaded correctly`() = runTest {
        val reservations = listOf(
            ReservationDto(id = "1", status = "pending"),
            ReservationDto(id = "2", status = "confirmed"),
            ReservationDto(id = "3", status = "completed"),
            ReservationDto(id = "4", status = "cancelled")
        )
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.reservations.size)

        // Verify all statuses are present
        val statuses = state.reservations.map { it.status }
        assertTrue(statuses.contains("pending"))
        assertTrue(statuses.contains("confirmed"))
        assertTrue(statuses.contains("completed"))
        assertTrue(statuses.contains("cancelled"))
    }

    @Test
    fun `loadReservations can be called multiple times`() = runTest {
        fakeReservationRepository.setReservations(listOf(
            ReservationDto(id = "1", status = "pending")
        ))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.reservations.size)

        // Update and reload
        fakeReservationRepository.setReservations(listOf(
            ReservationDto(id = "1", status = "pending"),
            ReservationDto(id = "2", status = "confirmed")
        ))

        viewModel.loadReservations()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.reservations.size)
    }

    @Test
    fun `error uses default message when result message is null`() = runTest {
        fakeReservationRepository.setShouldReturnError(true, null)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Fout bij ophalen reserveringen", viewModel.uiState.value.error)
    }

    @Test
    fun `cancel error uses default message when result message is null`() = runTest {
        val reservations = listOf(ReservationDto(id = "1", status = "pending"))
        fakeReservationRepository.setReservations(reservations)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeReservationRepository.setCancelShouldReturnError(true, null)

        viewModel.cancelReservation("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Fout bij annuleren reservering", viewModel.uiState.value.error)
    }
}

/**
 * Fake implementation of ReservationRepository for testing
 */
class FakeReservationRepository : ReservationRepository {

    private var reservations: List<ReservationDto> = emptyList()
    private var reservationsAfterCancel: List<ReservationDto>? = null
    private var shouldReturnError = false
    private var errorMessage: String? = null
    private var cancelShouldReturnError = false
    private var cancelErrorMessage: String? = null

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

    fun setCancelShouldReturnError(shouldError: Boolean, message: String? = null) {
        this.cancelShouldReturnError = shouldError
        this.cancelErrorMessage = message
    }

    override suspend fun getReservations(): Result<List<ReservationDto>> {
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
        if (cancelShouldReturnError) {
            return Result.Error(Exception("Cancel error"), cancelErrorMessage)
        }
        val existing = reservations.find { it.id == id }
        return if (existing != null) {
            // Update the reservations list to simulate backend removing/updating the cancelled reservation
            reservationsAfterCancel?.let { reservations = it }
            Result.Success(existing.copy(status = "cancelled"))
        } else {
            Result.Error(Exception("Reservation not found"))
        }
    }
}

/**
 * Fake implementation of CarRepository for testing reservations
 */
class FakeCarRepositoryForReservations : CarRepository {

    private var cars: List<CarDto> = emptyList()
    private var photosByCarId: MutableMap<String, List<PhotoDto>> = mutableMapOf()
    private var photosShouldReturnError = false

    fun setCars(cars: List<CarDto>) {
        this.cars = cars
    }

    fun setPhotos(carId: String, photos: List<PhotoDto>) {
        photosByCarId[carId] = photos
    }

    fun setPhotosShouldReturnError(shouldError: Boolean) {
        this.photosShouldReturnError = shouldError
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
        if (photosShouldReturnError) {
            return Result.Error(Exception("Photos error"))
        }
        return Result.Success(photosByCarId[carId] ?: emptyList())
    }
}