package com.example.rentmycar_android_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

@HiltAndroidApp
class RentMyCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize MapLibre
        MapLibre.getInstance(this)
        // Future: Add crash reporting, analytics, etc.
    }
}
