package com.example.rentmycar_android_app

import androidx.arch. core.executor.testing.InstantTaskExecutorRule
import com. example.rentmycar_android_app.domain.repository.UserRepository
import com.example.rentmycar_android_app. model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model. UserBonus
import com.example.rentmycar_android_app.network.SimpleResponse
import com.example.rentmycar_android_app.util.Result
import com.example.rentmycar_android_app.viewmodels.ProfileViewModel
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx. coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines. test.setMain
import org. junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit. Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var fakeUserRepository: FakeUserRepository

    @Before
    fun setup() {
        Dispatchers. setMain(testDispatcher)
        fakeUserRepository = FakeUserRepository()
        viewModel = ProfileViewModel(fakeUserRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_initialState_isCorrect() {
        val user = viewModel.user.value
        val bonus = viewModel.bonus.value
        val loading = viewModel. loading.value
        val error = viewModel.error.value

        assertNull("User moet null zijn bij start", user)
        assertNull("Bonus moet null zijn bij start", bonus)
        assertFalse("Loading moet false zijn bij start", loading)
        assertNull("Error moet null zijn bij start", error)
    }

    @Test
    fun test_loadProfile_success() {
        fakeUserRepository.shouldReturnError = false

        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val user = viewModel.user.value
        val bonus = viewModel.bonus.value
        val loading = viewModel.loading.value

        assertNotNull("User mag niet null zijn", user)
        assertEquals("John Doe", user?. name)
        assertEquals("john@example.com", user?.email)
        assertNotNull("Bonus mag niet null zijn", bonus)
        assertEquals(100, bonus?.totalPoints)
        assertFalse("Loading moet false zijn na laden", loading)
    }

    @Test
    fun test_loadProfile_userError() {
        fakeUserRepository.shouldReturnError = true
        fakeUserRepository.errorMessage = "Kan profiel niet laden"

        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.error.value
        val loading = viewModel.loading.value

        assertEquals("Kan profiel niet laden", error)
        assertFalse("Loading moet false zijn na error", loading)
    }

    @Test
    fun test_onNameChange_updatesUserName() {
        fakeUserRepository.shouldReturnError = false
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChange("Jane Doe")

        val user = viewModel.user.value
        assertEquals("Jane Doe", user?.name)
    }

    @Test
    fun test_onEmailChange_updatesUserEmail() {
        fakeUserRepository.shouldReturnError = false
        viewModel.loadProfile()
        testDispatcher.scheduler. advanceUntilIdle()

        viewModel.onEmailChange("jane@example.com")

        val user = viewModel.user.value
        assertEquals("jane@example.com", user?.email)
    }

    @Test
    fun test_onPhoneChange_updatesUserPhone() {
        fakeUserRepository.shouldReturnError = false
        viewModel.loadProfile()
        testDispatcher. scheduler.advanceUntilIdle()

        viewModel.onPhoneChange("0687654321")

        val user = viewModel.user.value
        assertEquals("0687654321", user?.phone)
    }

    @Test
    fun test_updateProfile_withEmptyName_showsError() {
        fakeUserRepository.shouldReturnError = false
        viewModel.loadProfile()
        testDispatcher.scheduler. advanceUntilIdle()

        viewModel.onNameChange("")
        viewModel.updateProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.error.value
        assertEquals("Naam en email zijn verplicht", error)
    }

    @Test
    fun test_updateProfile_withEmptyEmail_showsError() {
        fakeUserRepository.shouldReturnError = false
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEmailChange("")
        viewModel.updateProfile()
        testDispatcher.scheduler. advanceUntilIdle()

        val error = viewModel. error.value
        assertEquals("Naam en email zijn verplicht", error)
    }

    @Test
    fun test_updateProfile_success() {
        fakeUserRepository.shouldReturnError = false
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChange("Updated Name")
        viewModel.updateProfile()
        testDispatcher. scheduler.advanceUntilIdle()

        val error = viewModel.error.value
        assertEquals("Profiel succesvol bijgewerkt", error)
    }

    @Test
    fun test_updateProfile_withoutLoadingProfile_showsError() {
        viewModel.updateProfile()
        testDispatcher. scheduler.advanceUntilIdle()

        val error = viewModel.error.value
        assertEquals("Geen gebruikersgegevens gevonden", error)
    }
}

class FakeUserRepository : UserRepository {
    var shouldReturnError = false
    var errorMessage = "Error"

    override suspend fun getProfile(): Result<User> {
        return if (shouldReturnError) {
            Result.Error(
                exception = Exception(errorMessage),
                message = errorMessage
            )
        } else {
            Result.Success(
                User(
                    id = "1",
                    name = "John Doe",
                    email = "john@example.com",
                    phone = "0612345678",
                    password = ""
                )
            )
        }
    }

    override suspend fun getBonusPoints(): Result<UserBonus> {
        return if (shouldReturnError) {
            Result.Error(
                exception = Exception(errorMessage),
                message = errorMessage
            )
        } else {
            Result.Success(
                UserBonus(
                    id = "bonus1",
                    userId = "1",
                    userName = "John Doe",
                    totalPoints = 100,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<SimpleResponse> {
        return if (shouldReturnError) {
            Result.Error(
                exception = Exception(errorMessage),
                message = errorMessage
            )
        } else {
            Result.Success(
                SimpleResponse(
                    isSuccess = true,
                    message = "Profiel succesvol bijgewerkt"
                )
            )
        }
    }
}