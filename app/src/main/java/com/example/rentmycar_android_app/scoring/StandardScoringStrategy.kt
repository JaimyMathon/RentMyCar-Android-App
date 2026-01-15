package com.example.rentmycar_android_app.scoring

import kotlin.math.abs

/**
 * StandardScoringStrategy - ConcreteStrategy implementatie
 *
 * Standaard scoring algoritme met normale drempelwaarden.
 * Geschikt voor dagelijks gebruik en gemiddelde rijders.
 */
class StandardScoringStrategy : ScoringStrategy {

    override fun calculateLocalScore(
        maxAccel: Double,
        maxBraking: Double,
        harshAccelCount: Int,
        harshBrakeCount: Int
    ): Int {
        var score = 100

        // Acceleratie penalty
        when {
            abs(maxAccel) > 7.0 -> score -= 30
            abs(maxAccel) > 5.0 -> score -= 20
            abs(maxAccel) > 3.5 -> score -= 10
        }

        // Remmen penalty
        when {
            abs(maxBraking) > 7.0 -> score -= 30
            abs(maxBraking) > 5.0 -> score -= 20
            abs(maxBraking) > 3.5 -> score -= 10
        }

        // Penalty voor herhaalde harde bewegingen
        score -= (harshAccelCount * 5)
        score -= (harshBrakeCount * 5)

        return maxOf(0, score)
    }

    override fun getDescription(): String = "Standard Scoring (normale drempelwaarden)"
}