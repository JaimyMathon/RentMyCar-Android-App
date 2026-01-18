package com.example.rentmycar_android_app

import androidx. arch.core.executor.testing. InstantTaskExecutorRule
import com.example.rentmycar_android_app.domain.repository.AuthRepository
import com.example.rentmycar_android_app.network.AuthResponse
import com.example.rentmycar_android_app.network.RegisterRequest
import com.example.rentmycar_android_app. network.ResetPasswordRequest
import com.example.rentmycar_android_app.network.SimpleResponse
import com.example.rentmycar_android_app. ui.login.LoginViewModel
import com.example.rentmycar_android_app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test. StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org. junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi:: class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel:  LoginViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_initialState_isCorrect() {
        val state = viewModel.uiState. value
        assertFalse("Loading moet false zijn bij start", state.isLoading)
        assertNull("Error moet null zijn bij start", state.error)
        assertNull("LoginSuccess moet null zijn bij start", state.loginSuccess)
    }

    @Test
    fun test_clearError_removesError() {
        viewModel. login("", "")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()

        assertNull("Error moet null zijn na clearError", viewModel.uiState. value.error)
    }

    @Test
    fun test_login_withValidCredentials_succeeds() {
        fakeAuthRepository.shouldReturnError = false
        fakeAuthRepository.username = "TestUser"

        viewModel.login("test@example. com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Loading moet false zijn na success", state.isLoading)
        assertNull("Error moet null zijn bij success", state.error)
        assertEquals("TestUser", state. loginSuccess)
    }

    @Test
    fun test_login_withInvalidCredentials_showsError() {
        fakeAuthRepository.shouldReturnError = true
        fakeAuthRepository. errorMessage = "E-mail of wachtwoord onjuist"

        viewModel.login("wrong@example.com", "wrongpass")
        testDispatcher. scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("E-mail of wachtwoord onjuist", state.error)
        assertNull("LoginSuccess moet null zijn bij fout credentials", state.loginSuccess)
    }

    @Test
    fun test_login_withEmptyEmail_showsError() {
        fakeAuthRepository.shouldReturnError = true
        fakeAuthRepository.errorMessage = "E-mail is verplicht"

        viewModel.login("", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState. value
        assertNotNull("Error mag niet null zijn", state.error)
        assertNull("LoginSuccess moet null zijn bij error", state. loginSuccess)
    }
}

class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false
    var errorMessage = "Error"
    var username = "TestUser"

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return if (shouldReturnError) {
            Result.Error(
                exception = Exception(errorMessage),
                message = errorMessage
            )
        } else {
            Result.Success(
                AuthResponse(
                    token = "fake-token-123",
                    username = username,
                    expiresAt = "2025-12-31T23:59:59Z"
                )
            )
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return Result.Success(
            AuthResponse(
                token = "fake-token",
                username = "TestUser",
                expiresAt = "2025-12-31T23:59:59Z"
            )
        )
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<SimpleResponse> {
        return Result.Success(
            SimpleResponse(
                isSuccess = true,
                message = "Password reset successful"
            )
        )
    }

    override suspend fun logout() {
    }
}