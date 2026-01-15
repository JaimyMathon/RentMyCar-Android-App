package com.example.rentmycar_android_app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.example.rentmycar_android_app.domain.repository.CarRepository
import com.example.rentmycar_android_app.network.AddCarRequest
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.network.PhotoDto
import com.example.rentmycar_android_app.util.Result
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

        // Verify title is displayed
        composeTestRule.onNodeWithText("Filter").assertIsDisplayed()

        // Verify section titles
        composeTestRule.onNodeWithText("Types").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prijs per Km").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prijs per dag").assertIsDisplayed()
        composeTestRule.onNodeWithText("Merken").assertIsDisplayed()

        // Verify type chips are displayed
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("ICE").assertIsDisplayed()
        composeTestRule.onNodeWithText("BEV").assertIsDisplayed()
        composeTestRule.onNodeWithText("FCEV").assertIsDisplayed()

        // Verify action buttons
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

        // Click on BEV chip
        composeTestRule.onNodeWithText("BEV").performClick()

        // Verify the selection is updated in ViewModel
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

        // Click on BEV and ICE chips
        composeTestRule.onNodeWithText("BEV").performClick()
        composeTestRule.onNodeWithText("ICE").performClick()

        // Verify both selections are updated in ViewModel
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

        // First select a type
        composeTestRule.onNodeWithText("BEV").performClick()
        assertTrue(viewModel.getCurrentFilterState().selectedTypes.contains("BEV"))

        // Click on All chip
        composeTestRule.onNodeWithText("All").performClick()

        // Verify selection is cleared
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

        // Select some filters
        composeTestRule.onNodeWithText("BEV").performClick()
        assertTrue(viewModel.getCurrentFilterState().selectedTypes.isNotEmpty())

        // Click reset button
        composeTestRule.onNodeWithText("Reset").performClick()

        // Verify all filters are reset
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

        // Select a filter
        composeTestRule.onNodeWithText("FCEV").performClick()

        // Click apply button
        composeTestRule.onNodeWithText("Toepassen").performClick()

        // Verify filters were applied
        assertNotNull(appliedFilterState)
        assertTrue(appliedFilterState!!.selectedTypes.contains("FCEV"))

        // Verify back navigation was triggered
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

        // Click back button
        composeTestRule.onNodeWithContentDescription("Terug").performClick()

        // Verify back navigation was triggered
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

        // Wait for brands to load
        composeTestRule.waitForIdle()

        // Verify brands from repository are displayed (sorted alphabetically)
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

        // Wait for brands to load
        composeTestRule.waitForIdle()

        // Click on a brand
        composeTestRule.onNodeWithText("Tesla").performClick()

        // Verify selection
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

        // Verify initial state is applied
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