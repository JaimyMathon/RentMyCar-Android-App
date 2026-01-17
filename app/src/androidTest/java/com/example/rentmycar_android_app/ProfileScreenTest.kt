package com.example.rentmycar_android_app

import ProfileScreen
import androidx.compose.ui.test.*
import androidx.compose.ui. test.junit4.createComposeRule
import com.example. rentmycar_android_app. domain.repository.UserRepository
import com.example.rentmycar_android_app. model.UpdateProfileRequest
import com.example.rentmycar_android_app.model.User
import com.example.rentmycar_android_app.model. UserBonus
import com.example.rentmycar_android_app.network.SimpleResponse
import com. example.rentmycar_android_app.util.Result
import com.example.rentmycar_android_app.viewmodels.ProfileViewModel
import org.junit.Rule
import org.junit. Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_profileScreen_displaysAllElements() {
        val fakeRepo = FakeUserRepository()
        val viewModel = ProfileViewModel(fakeRepo)

        composeTestRule. setContent {
            ProfileScreen(
                token = "fake-token",
                onLogout = {},
                onNavigateBack = {},
                onNavigateToDrivingStats = {},
                viewModel = viewModel
            )
        }

        composeTestRule. waitForIdle()

        composeTestRule.onNodeWithText("Profiel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jouw Bonuspunten").assertIsDisplayed()
        composeTestRule. onNodeWithText("100").assertIsDisplayed()
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gegevens opslaan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uitloggen").assertIsDisplayed()
    }

    @Test
    fun test_userFields_canBeEdited() {
        val fakeRepo = FakeUserRepository()
        val viewModel = ProfileViewModel(fakeRepo)

        composeTestRule.setContent {
            ProfileScreen(
                token = "fake-token",
                onLogout = {},
                onNavigateBack = {},
                onNavigateToDrivingStats = {},
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("John Doe").performTextClearance()
        composeTestRule.onNodeWithText("Uw naam").performTextInput("Jane Doe")
        composeTestRule. onNodeWithText("Jane Doe").assertIsDisplayed()
    }

    @Test
    fun test_saveButton_showsSuccessMessage() {
        val fakeRepo = FakeUserRepository()
        val viewModel = ProfileViewModel(fakeRepo)

        composeTestRule.setContent {
            ProfileScreen(
                token = "fake-token",
                onLogout = {},
                onNavigateBack = {},
                onNavigateToDrivingStats = {},
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule. onNodeWithText("Gegevens opslaan").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Profiel succesvol bijgewerkt", substring = true).assertIsDisplayed()
    }

    @Test
    fun test_logoutButton_opensDialog() {
        val fakeRepo = FakeUserRepository()
        val viewModel = ProfileViewModel(fakeRepo)

        composeTestRule.setContent {
            ProfileScreen(
                token = "fake-token",
                onLogout = {},
                onNavigateBack = {},
                onNavigateToDrivingStats = {},
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Uitloggen").performClick()

        composeTestRule.onNodeWithText("Weet je zeker dat je wilt uitloggen?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Annuleren").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ja, Uitloggen").assertIsDisplayed()
    }

    @Test
    fun test_logoutDialog_confirmButton_callsOnLogout() {
        val fakeRepo = FakeUserRepository()
        val viewModel = ProfileViewModel(fakeRepo)
        var logoutCalled = false

        composeTestRule.setContent {
            ProfileScreen(
                token = "fake-token",
                onLogout = { logoutCalled = true },
                onNavigateBack = {},
                onNavigateToDrivingStats = {},
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule. onNodeWithText("Uitloggen").performClick()
        composeTestRule.onNodeWithText("Ja, Uitloggen").performClick()

        assert(logoutCalled)
    }
}

class FakeUserRepository :  UserRepository {
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
                    email = "john@example. com",
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