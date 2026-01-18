package com.example.rentmycar_android_app

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui. test.junit4.createComposeRule
import com.example.rentmycar_android_app. domain.repository.AuthRepository
import com.example.rentmycar_android_app. network.AuthResponse
import com. example.rentmycar_android_app.network.RegisterRequest
import com.example.rentmycar_android_app.network. ResetPasswordRequest
import com. example.rentmycar_android_app.network.SimpleResponse
import com.example.rentmycar_android_app.ui. LoginScreen
import com.example. rentmycar_android_app.ui.login.LoginViewModel
import com.example.rentmycar_android_app.util. Result
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_loginScreen_displaysAllElements() {
        val fakeViewModel = LoginViewModel(FakeAuthRepository())

        composeTestRule. setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                viewModel = fakeViewModel
            )
        }

        composeTestRule.onNodeWithText("Rent My Car").assertIsDisplayed()
        composeTestRule. onNodeWithText("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wachtwoord").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inloggen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wachtwoord vergeten?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nog geen account? Registreer hier").assertIsDisplayed()
    }

    @Test
    fun test_emailInput_canBeTyped() {
        val fakeViewModel = LoginViewModel(FakeAuthRepository())

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                viewModel = fakeViewModel
            )
        }

        composeTestRule.onNodeWithText("E-mail").performTextInput("test@example.com")
        composeTestRule. onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun test_passwordInput_canBeTyped() {
        val fakeViewModel = LoginViewModel(FakeAuthRepository())

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                viewModel = fakeViewModel
            )
        }

        composeTestRule.onNodeWithText("Wachtwoord").performTextInput("password123")
        composeTestRule. onNodeWithText("Wachtwoord").assertExists()
    }

    @Test
    fun test_loginButton_isClickable() {
        val fakeViewModel = LoginViewModel(FakeAuthRepository())
        var loginClicked = false

        composeTestRule. setContent {
            LoginScreen(
                onLoginSuccess = { loginClicked = true },
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                viewModel = fakeViewModel
            )
        }

        composeTestRule. onNodeWithText("E-mail").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Wachtwoord").performTextInput("password123")
        composeTestRule.onNodeWithText("Inloggen").performClick()

        composeTestRule. onNodeWithText("Inloggen").assertExists()
    }

    @Test
    fun test_errorMessage_isDisplayed() {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.shouldReturnError = true
        fakeRepo.errorMessage = "E-mail of wachtwoord onjuist"
        val fakeViewModel = LoginViewModel(fakeRepo)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                viewModel = fakeViewModel
            )
        }

        composeTestRule.onNodeWithText("E-mail").performTextInput("wrong@example.com")
        composeTestRule.onNodeWithText("Wachtwoord").performTextInput("wrongpass")
        composeTestRule. onNodeWithText("Inloggen").performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("E-mail of wachtwoord onjuist")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("E-mail of wachtwoord onjuist").assertIsDisplayed()
    }

    @Test
    fun test_loadingIndicator_appearsWhenLoading() {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.shouldDelay = true
        val fakeViewModel = LoginViewModel(fakeRepo)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                viewModel = fakeViewModel
            )
        }

        composeTestRule.onNodeWithText("E-mail").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Wachtwoord").performTextInput("password123")
        composeTestRule.onNodeWithText("Inloggen").performClick()

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }
}

class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false
    var errorMessage = "Error"
    var shouldDelay = false

    override suspend fun login(email:  String, password: String): Result<AuthResponse> {
        if (shouldDelay) {
            kotlinx.coroutines.delay(5000)
        }

        return if (shouldReturnError) {
            Result.Error(
                exception = Exception(errorMessage),
                message = errorMessage
            )
        } else {
            Result.Success(
                AuthResponse(
                    token = "fake-token-123",
                    username = "TestUser",
                    expiresAt = "2025-12-31T23:59:59Z"
                )
            )
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return Result. Success(
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

    override suspend fun logout() {}
}