package com.example.rentmycar_android_app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rentmycar_android_app.network.CarDto
import com.example.rentmycar_android_app.ui.payment.PaymentReviewUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentReviewScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun baseArgs(
        uiState: PaymentReviewUiState,
        onPayClick: () -> Unit = {},
        onPaymentSuccess: () -> Unit = {}
    ) {
        composeRule.setContent {
            PaymentReviewScreenContent(
                carId = "car123",
                fromDate = "01-01-2024",
                toDate = "05-01-2024",
                kms = "100",
                paymentMethod = "Paypal",
                uiState = uiState,
                onBackClick = {},
                onPaymentSuccess = onPaymentSuccess,
                onPayClick = onPayClick
            )
        }
    }

    // ============================================================
    // UI TEST 1 (rubric-proof): loading zichtbaar + betalen disabled
    // ============================================================
    @Test
    fun loading_showsProgress_andPayButtonDisabled() {
        baseArgs(
            uiState = PaymentReviewUiState(
                isLoading = true,
                error = null,
                car = null,
                isProcessing = false,
                paymentSuccess = false
            )
        )

        composeRule.onNodeWithTag("payment_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("payment_pay_button").assertIsNotEnabled()
    }

    // ============================================================
    // UI TEST 2 (rubric-proof): car geladen -> betalen enabled + click
    // ============================================================
    @Test
    fun carLoaded_enablesPayButton_andClickCallsOnPayClick() {
        var payClicks = 0

        val car = CarDto(
            id = "car123",
            brand = "Tesla",
            model = "Model 3",
            pricePerTimeSlot = 50.0,
            tco = 3650.0,
            costPerKm = 0.25
        )

        baseArgs(
            uiState = PaymentReviewUiState(
                isLoading = false,
                error = null,
                car = car,
                isProcessing = false,
                paymentSuccess = false
            ),
            onPayClick = { payClicks++ }
        )

        composeRule.onNodeWithTag("payment_pay_button").assertIsEnabled()
        composeRule.onNodeWithTag("payment_pay_button").performClick()

        assertEquals(1, payClicks)
    }

    // ============================================================
    // EXTRA UI TEST 3: processing -> spinner zichtbaar + button disabled
    // ============================================================
    @Test
    fun processing_showsSpinnerInsideButton_andDisablesButton() {
        val car = CarDto(
            id = "car123",
            brand = "Tesla",
            model = "Model 3",
            pricePerTimeSlot = 50.0,
            tco = 3650.0,
            costPerKm = 0.25
        )

        baseArgs(
            uiState = PaymentReviewUiState(
                isLoading = false,
                error = null,
                car = car,
                isProcessing = true,
                paymentSuccess = false
            )
        )

        composeRule.onNodeWithTag("payment_processing").assertIsDisplayed()
        composeRule.onNodeWithTag("payment_pay_button").assertIsNotEnabled()
    }

    // ============================================================
    // EXTRA UI TEST 4: paymentSuccess triggert onPaymentSuccess
    // ============================================================
    @Test
    fun paymentSuccess_triggersOnPaymentSuccessCallback() {
        var successCalls = 0

        baseArgs(
            uiState = PaymentReviewUiState(
                isLoading = false,
                error = null,
                car = null,
                isProcessing = false,
                paymentSuccess = true
            ),
            onPaymentSuccess = { successCalls++ }
        )

        // LaunchedEffect draait async; give Compose een tick
        composeRule.waitForIdle()

        assertTrue(successCalls >= 1)
    }
}