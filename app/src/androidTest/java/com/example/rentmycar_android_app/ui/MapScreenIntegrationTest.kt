package com.example.rentmycar_android_app.ui

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for MapScreen that verify the UI components
 * and user interactions around the map functionality.
 *
 * Note: The actual MapLibre map view is a native Android View and
 * cannot be fully tested with Compose testing. These tests focus on
 * the Compose UI elements: TopAppBar, permission UI, FAB, and navigation.
 */
class MapScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var backClicked = false

    // Test coordinates (Amsterdam)
    private val testCarLatitude = 52.3676
    private val testCarLongitude = 4.9041

    @Before
    fun setup() {
        backClicked = false
    }

    // ==================== TopAppBar Tests ====================

    @Test
    fun mapScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.onNodeWithText("Route naar auto").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysBackButton() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.onNodeWithContentDescription("Terug").assertIsDisplayed()
    }

    @Test
    fun mapScreen_backButton_triggersNavigation() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.onNodeWithContentDescription("Terug").performClick()

        assertTrue(backClicked)
    }

    // ==================== Navigation FAB Tests ====================

    @Test
    fun mapScreen_withPermission_displaysFAB() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.waitForIdle()

        // FAB should be visible when permission is granted
        // Initial state is "navigating" so content description should be "Stop navigatie"
        composeTestRule.onNodeWithContentDescription("Stop navigatie").assertExists()
    }

    @Test
    fun mapScreen_fabClick_togglesNavigationState() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.waitForIdle()

        // Initially navigating, FAB shows "Stop navigatie"
        val fabNode = composeTestRule.onNodeWithContentDescription("Stop navigatie")
        fabNode.assertExists()

        // Click FAB to toggle
        fabNode.performClick()
        composeTestRule.waitForIdle()

        // Now should show "Start navigatie"
        composeTestRule.onNodeWithContentDescription("Start navigatie").assertExists()
    }

    @Test
    fun mapScreen_fabDoubleClick_returnsToOriginalState() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.waitForIdle()

        // Click FAB twice
        composeTestRule.onNodeWithContentDescription("Stop navigatie").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Start navigatie").performClick()
        composeTestRule.waitForIdle()

        // Should be back to "Stop navigatie"
        composeTestRule.onNodeWithContentDescription("Stop navigatie").assertExists()
    }

    // ==================== Coordinate Parameter Tests ====================

    @Test
    fun mapScreen_acceptsDifferentCoordinates_rotterdamLocation() {
        // Rotterdam coordinates
        val rotterdamLat = 51.9244
        val rotterdamLon = 4.4777

        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = rotterdamLat,
                carLongitude = rotterdamLon
            )
        }

        // Screen should render without errors
        composeTestRule.onNodeWithText("Route naar auto").assertIsDisplayed()
    }

    @Test
    fun mapScreen_acceptsDifferentCoordinates_utrechtLocation() {
        // Utrecht coordinates
        val utrechtLat = 52.0907
        val utrechtLon = 5.1214

        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = utrechtLat,
                carLongitude = utrechtLon
            )
        }

        // Screen should render without errors
        composeTestRule.onNodeWithText("Route naar auto").assertIsDisplayed()
    }

    @Test
    fun mapScreen_handlesZeroCoordinates() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = 0.0,
                carLongitude = 0.0
            )
        }

        // Screen should still render
        composeTestRule.onNodeWithText("Route naar auto").assertIsDisplayed()
    }

    @Test
    fun mapScreen_handlesNegativeCoordinates() {
        // Coordinates in southern/western hemisphere
        val southLat = -33.8688 // Sydney
        val westLon = -151.2093

        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = southLat,
                carLongitude = westLon
            )
        }

        // Screen should render without errors
        composeTestRule.onNodeWithText("Route naar auto").assertIsDisplayed()
    }

    // ==================== UI State Tests ====================

    @Test
    fun mapScreen_initialState_showsNavigatingMode() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        composeTestRule.waitForIdle()

        // Initial state should be navigating (isNavigating = true)
        composeTestRule.onNodeWithContentDescription("Stop navigatie").assertExists()
    }

    // ==================== Multiple Back Click Prevention Test ====================

    @Test
    fun mapScreen_multipleBackClicks_triggersCallbackMultipleTimes() {
        var clickCount = 0

        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { clickCount++ },
                carLatitude = testCarLatitude,
                carLongitude = testCarLongitude
            )
        }

        // Click back button multiple times
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("Terug").performClick()
        }

        assertEquals(3, clickCount)
    }
}

/**
 * Tests for MapScreen without location permissions.
 * These tests verify the permission denied UI flow.
 */
class MapScreenPermissionDeniedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Note: Without GrantPermissionRule, permissions are not granted
    // However, in emulators/devices with permissions already granted system-wide,
    // these tests may behave differently.

    private var backClicked = false

    @Before
    fun setup() {
        backClicked = false
    }

    @Test
    fun mapScreen_displaysTitle_regardlessOfPermission() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = 52.3676,
                carLongitude = 4.9041
            )
        }

        // Title should always be displayed
        composeTestRule.onNodeWithText("Route naar auto").assertIsDisplayed()
    }

    @Test
    fun mapScreen_backButton_worksRegardlessOfPermission() {
        composeTestRule.setContent {
            MapScreen(
                onNavigateBack = { backClicked = true },
                carLatitude = 52.3676,
                carLongitude = 4.9041
            )
        }

        composeTestRule.onNodeWithContentDescription("Terug").performClick()

        assertTrue(backClicked)
    }
}