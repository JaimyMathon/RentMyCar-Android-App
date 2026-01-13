package com.example.rentmycar_android_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RentMyCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Future: Add crash reporting, analytics, etc.
    }
}
