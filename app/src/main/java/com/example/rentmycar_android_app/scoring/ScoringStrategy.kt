package com.example.rentmycar_android_app.scoring

interface ScoringStrategy {
    fun calculateLocalScore(
        maxAccel: Double,
        maxBraking: Double,
        harshAccelCount: Int,
        harshBrakeCount: Int
    ): Int
}