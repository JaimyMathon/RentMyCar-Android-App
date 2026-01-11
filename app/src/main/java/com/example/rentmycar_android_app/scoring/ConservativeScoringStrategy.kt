package com.example.rentmycar_android_app.scoring

import kotlin. math.abs

class ConservativeScoringStrategy : ScoringStrategy {
    override fun calculateLocalScore(
        maxAccel:  Double,
        maxBraking: Double,
        harshAccelCount: Int,
        harshBrakeCount: Int
    ): Int {
        var score = 100

        when {
            abs(maxAccel) > 6.0 -> score -= 40
            abs(maxAccel) > 4.0 -> score -= 25
            abs(maxAccel) > 2.5 -> score -= 15
        }

        when {
            abs(maxBraking) > 6.0 -> score -= 40
            abs(maxBraking) > 4.0 -> score -= 25
            abs(maxBraking) > 2.5 -> score -= 15
        }

        score -= (harshAccelCount * 8)
        score -= (harshBrakeCount * 8)

        return maxOf(0, score)
    }
}