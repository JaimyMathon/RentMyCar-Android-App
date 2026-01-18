package com.example.rentmycar_android_app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.util.Result
import com.example.rentmycar_android_app.viewmodels.FilterViewModel
import java.io.File
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeCarRepository: FakeCarRepositoryForFilter
    private lateinit var viewModel: FilterViewModel
    private var appliedFilterState: FilterState? = null
    private var backClicked = false

    @Before
    fun setup() {
        fakeCarRepository = FakeCarRepositoryForFilter()
        fakeCarRepository.setCars(listOf(
            CarDto(id = "1", brand = "Tesla", model = "Model 3"),
            CarDto(id = "2", brand = "BMW", model = "i4"),
            CarDto(id = "3", brand = "Audi", model = "e-tron")
        ))
        viewModel = FilterViewModel(fakeCarRepository, SavedStateHandle())
        appliedFilterState = null
        backClicked = false
    }

    @Test
    fun filterScreen_displaysAllElements() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Filter").assertIsDisplayed()

        composeTestRule.onNodeWithText("Types").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prijs per Km").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prijs per dag").assertIsDisplayed()
        composeTestRule.onNodeWithText("Merken").assertIsDisplayed()

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("ICE").assertIsDisplayed()
        composeTestRule.onNodeWithText("BEV").assertIsDisplayed()
        composeTestRule.onNodeWithText("FCEV").assertIsDisplayed()

        composeTestRule.onNodeWithText("Reset").assertIsDisplayed()
        composeTestRule.onNodeWithText("Toepassen").assertIsDisplayed()
    }

    @Test
    fun filterScreen_selectTypeChip_updatesSelection() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("BEV").performClick()

        assertTrue(viewModel.getCurrentFilterState().selectedTypes.contains("BEV"))
    }

    @Test
    fun filterScreen_selectMultipleTypeChips_updatesSelection() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("BEV").performClick()
        composeTestRule.onNodeWithText("ICE").performClick()

        val selectedTypes = viewModel.getCurrentFilterState().selectedTypes
        assertTrue(selectedTypes.contains("BEV"))
        assertTrue(selectedTypes.contains("ICE"))
    }

    @Test
    fun filterScreen_clickAllChip_clearsSelection() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("BEV").performClick()
        assertTrue(viewModel.getCurrentFilterState().selectedTypes.contains("BEV"))

        composeTestRule.onNodeWithText("All").performClick()

        assertTrue(viewModel.getCurrentFilterState().selectedTypes.isEmpty())
    }

    @Test
    fun filterScreen_resetButton_clearsAllFilters() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("BEV").performClick()
        assertTrue(viewModel.getCurrentFilterState().selectedTypes.isNotEmpty())

        composeTestRule.onNodeWithText("Reset").performClick()

        val filterState = viewModel.getCurrentFilterState()
        assertTrue(filterState.selectedTypes.isEmpty())
        assertEquals(0.70f, filterState.maxPricePerKm)
        assertEquals(300f, filterState.maxPricePerDay)
        assertTrue(filterState.selectedBrands.isEmpty())
    }

    @Test
    fun filterScreen_applyButton_appliesFiltersAndNavigatesBack() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("FCEV").performClick()

        composeTestRule.onNodeWithText("Toepassen").performClick()

        assertNotNull(appliedFilterState)
        assertTrue(appliedFilterState!!.selectedTypes.contains("FCEV"))

        assertTrue(backClicked)
    }

    @Test
    fun filterScreen_backButton_navigatesBack() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Terug").performClick()

        assertTrue(backClicked)
    }

    @Test
    fun filterScreen_displaysBrandsFromRepository() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Audi").assertIsDisplayed()
        composeTestRule.onNodeWithText("BMW").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tesla").assertIsDisplayed()
    }

    @Test
    fun filterScreen_selectBrand_updatesSelection() {
        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Tesla").performClick()

        assertTrue(viewModel.getCurrentFilterState().selectedBrands.contains("Tesla"))
    }

    @Test
    fun filterScreen_initialFilterState_isApplied() {
        val initialState = FilterState(
            selectedTypes = setOf("BEV"),
            maxPricePerKm = 0.50f,
            maxPricePerDay = 200f,
            selectedBrands = setOf("Tesla")
        )

        composeTestRule.setContent {
            FilterScreen(
                onBackClick = { backClicked = true },
                onApplyFilters = { appliedFilterState = it },
                initialFilterState = initialState,
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        val currentState = viewModel.getCurrentFilterState()
        assertTrue(currentState.selectedTypes.contains("BEV"))
        assertEquals(0.50f, currentState.maxPricePerKm)
        assertEquals(200f, currentState.maxPricePerDay)
        assertTrue(currentState.selectedBrands.contains("Tesla"))
    }
}

/**
 * Fake implementation of CarRepository for Filter testing
 */
class FakeCarRepositoryForFilter : CarRepository {

    private var cars: List<CarDto> = emptyList()

    fun setCars(cars: List<CarDto>) {
        this.cars = cars
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
        return Result.Success(emptyList())
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