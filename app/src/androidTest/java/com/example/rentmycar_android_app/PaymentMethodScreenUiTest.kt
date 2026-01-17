package com.example.rentmycar_android_app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests - PaymentMethodScreen
 *
 * Gedekt:
 * 1) Continue knop is disabled zolang geen betaalmethode geselecteerd is.
 * 2) Selectie van een betaalmethode activeert knop en callback krijgt juiste methode.
 */
@RunWith(AndroidJUnit4::class)
class PaymentMethodScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var continueSelection: PaymentMethod? = null

    @Before
    fun setup() {
        continueSelection = null

        composeRule.setContent {
            PaymentMethodScreen(
                onBackClick = {},
                onContinueClick = { method -> continueSelection = method }
            )
        }
    }

    @Test
    fun continueButton_disabled_whenNothingSelected() {
        composeRule.onNodeWithText("naar betaal overzicht").assertIsNotEnabled()
        assertNull(continueSelection)
    }

    @Test
    fun selectingPaypal_enablesContinue_andPassesPaypalToCallback() {
        composeRule.onNodeWithText("Paypal").performClick()
        composeRule.onNodeWithText("naar betaal overzicht").assertIsEnabled()

        composeRule.onNodeWithText("naar betaal overzicht").performClick()
        assertEquals(PaymentMethod.Paypal, continueSelection)
    }
}