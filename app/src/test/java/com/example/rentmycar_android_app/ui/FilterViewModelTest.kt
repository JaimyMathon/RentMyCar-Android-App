package com.example.rentmycar_android_app.ui

import androidx.lifecycle.SavedStateHandle
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.util.Result
import com.example.rentmycar_android_app.viewmodels.FilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilterViewModelTest {

    private lateinit var viewModel: FilterViewModel
    private lateinit var fakeCarRepository: FakeCarRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCarRepository = FakeCarRepository()
        viewModel = FilterViewModel(fakeCarRepository, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial filter state has default values`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.filterState.value

        assertEquals(emptySet<String>(), state.selectedTypes)
        assertEquals(0.70f, state.maxPricePerKm, 0.01f)
        assertEquals(300f, state.maxPricePerDay, 0.01f)
        assertEquals(emptySet<String>(), state.selectedBrands)
    }

    @Test
    fun `initializeFilter sets filter state correctly`() = runTest {
        val initialState = FilterState(
            selectedTypes = setOf("BEV", "ICE"),
            maxPricePerKm = 0.50f,
            maxPricePerDay = 150f,
            selectedBrands = setOf("Tesla", "BMW")
        )

        viewModel.initializeFilter(initialState)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.filterState.value
        assertEquals(setOf("BEV", "ICE"), state.selectedTypes)
        assertEquals(0.50f, state.maxPricePerKm, 0.01f)
        assertEquals(150f, state.maxPricePerDay, 0.01f)
        assertEquals(setOf("Tesla", "BMW"), state.selectedBrands)
    }

    @Test
    fun `updateSelectedTypes updates types correctly`() = runTest {
        viewModel.updateSelectedTypes(setOf("ICE", "BEV"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(setOf("ICE", "BEV"), viewModel.filterState.value.selectedTypes)
    }

    @Test
    fun `updateSelectedTypes with empty set clears types`() = runTest {
        viewModel.updateSelectedTypes(setOf("ICE"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSelectedTypes(emptySet())
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptySet<String>(), viewModel.filterState.value.selectedTypes)
    }

    @Test
    fun `updateMaxPricePerKm updates price correctly`() = runTest {
        viewModel.updateMaxPricePerKm(0.35f)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0.35f, viewModel.filterState.value.maxPricePerKm, 0.01f)
    }

    @Test
    fun `updateMaxPricePerDay updates price correctly`() = runTest {
        viewModel.updateMaxPricePerDay(200f)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(200f, viewModel.filterState.value.maxPricePerDay, 0.01f)
    }

    @Test
    fun `updateSelectedBrands updates brands correctly`() = runTest {
        viewModel.updateSelectedBrands(setOf("Honda", "Toyota"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(setOf("Honda", "Toyota"), viewModel.filterState.value.selectedBrands)
    }

    @Test
    fun `resetFilters resets to default state`() = runTest {
        // First set some custom values
        viewModel.updateSelectedTypes(setOf("BEV"))
        viewModel.updateMaxPricePerKm(0.40f)
        viewModel.updateMaxPricePerDay(100f)
        viewModel.updateSelectedBrands(setOf("Tesla"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Reset filters
        viewModel.resetFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.filterState.value
        assertEquals(emptySet<String>(), state.selectedTypes)
        assertEquals(0.70f, state.maxPricePerKm, 0.01f)
        assertEquals(300f, state.maxPricePerDay, 0.01f)
        assertEquals(emptySet<String>(), state.selectedBrands)
    }

    @Test
    fun `getCurrentFilterState returns current state`() = runTest {
        viewModel.updateSelectedTypes(setOf("FCEV"))
        viewModel.updateMaxPricePerKm(0.25f)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.getCurrentFilterState()

        assertEquals(setOf("FCEV"), state.selectedTypes)
        assertEquals(0.25f, state.maxPricePerKm, 0.01f)
    }

    @Test
    fun `availableBrands loads brands from repository`() = runTest {
        fakeCarRepository.setCars(listOf(
            CarDto(id = "1", brand = "Tesla"),
            CarDto(id = "2", brand = "BMW"),
            CarDto(id = "3", brand = "Tesla"), // duplicate
            CarDto(id = "4", brand = "Audi")
        ))

        // Create new ViewModel to trigger init block
        val newViewModel = FilterViewModel(fakeCarRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val brands = newViewModel.availableBrands.value
        assertEquals(3, brands.size)
        assertTrue(brands.contains("Tesla"))
        assertTrue(brands.contains("BMW"))
        assertTrue(brands.contains("Audi"))
    }

    @Test
    fun `availableBrands are sorted alphabetically`() = runTest {
        fakeCarRepository.setCars(listOf(
            CarDto(id = "1", brand = "Volkswagen"),
            CarDto(id = "2", brand = "Audi"),
            CarDto(id = "3", brand = "Mercedes")
        ))

        val newViewModel = FilterViewModel(fakeCarRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val brands = newViewModel.availableBrands.value
        assertEquals(listOf("Audi", "Mercedes", "Volkswagen"), brands)
    }

    @Test
    fun `availableBrands filters out blank brands`() = runTest {
        fakeCarRepository.setCars(listOf(
            CarDto(id = "1", brand = "Tesla"),
            CarDto(id = "2", brand = ""),
            CarDto(id = "3", brand = "   "),
            CarDto(id = "4", brand = null)
        ))

        val newViewModel = FilterViewModel(fakeCarRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val brands = newViewModel.availableBrands.value
        assertEquals(1, brands.size)
        assertEquals("Tesla", brands.first())
    }

    @Test
    fun `availableBrands uses fallback list on error`() = runTest {
        fakeCarRepository.setShouldReturnError(true)

        val newViewModel = FilterViewModel(fakeCarRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val brands = newViewModel.availableBrands.value
        assertEquals(listOf("Honda", "Nissan", "Audi", "Mercedes"), brands)
    }

    @Test
    fun `multiple filter updates preserve other values`() = runTest {
        viewModel.updateSelectedTypes(setOf("BEV"))
        viewModel.updateMaxPricePerKm(0.30f)
        viewModel.updateMaxPricePerDay(150f)
        viewModel.updateSelectedBrands(setOf("Tesla"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Update only types
        viewModel.updateSelectedTypes(setOf("ICE"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.filterState.value
        assertEquals(setOf("ICE"), state.selectedTypes)
        assertEquals(0.30f, state.maxPricePerKm, 0.01f)
        assertEquals(150f, state.maxPricePerDay, 0.01f)
        assertEquals(setOf("Tesla"), state.selectedBrands)
    }
}

/**
 * Fake implementation of CarRepository for testing
 */
class FakeCarRepository : CarRepository {

    private var cars: List<CarDto> = emptyList()
    private var shouldReturnError = false

    fun setCars(cars: List<CarDto>) {
        this.cars = cars
    }

    fun setShouldReturnError(shouldError: Boolean) {
        this.shouldReturnError = shouldError
    }

    override suspend fun getCars(): Result<List<CarDto>> {
        return if (shouldReturnError) {
            Result.Error(Exception("Test error"))
        } else {
            Result.Success(cars)
        }
    }

    override suspend fun getCarById(id: String): Result<CarDto> {
        val car = cars.find { it.id == id }
        return if (car != null) {
            Result.Success(car)
        } else {
            Result.Error(Exception("Car not found"))
        }
    }

    override suspend fun getCarsByOwner(ownerId: String): Result<List<CarDto>> {
        return Result.Success(cars.filter { it.ownerId == ownerId })
    }

    override suspend fun addCar(request: com.example.rentmycar_android_app.network.AddCarRequest): Result<CarDto> {
        return Result.Success(CarDto(id = "new-car", brand = request.brand, model = request.model))
    }

    override suspend fun addPhoto(carId: String, description: String, file: java.io.File): Result<PhotoDto> {
        return Result.Success(PhotoDto(url = "/photos/$carId/photo.jpg", carId = carId))
    }

    override suspend fun updateCar(id: String, request: com.example.rentmycar_android_app.network.AddCarRequest): Result<CarDto> {
        return Result.Success(CarDto(id = id, brand = request.brand, model = request.model))
    }

    override suspend fun deleteCar(id: String): Result<Boolean> {
        return Result.Success(true)
    }

    override suspend fun geocodeAddress(street: String, postcode: String, country: String): Result<Pair<Double, Double>> {
        return Result.Success(Pair(52.0, 4.0))
    }

    override suspend fun getCarPhotos(carId: String): Result<List<PhotoDto>> {
        return Result.Success(emptyList())
    }
}