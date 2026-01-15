package com.example.rentmycar_android_app.scoring

/**
 * =============================================================================
 * DESIGN PATTERN 2: STRATEGY PATTERN
 * =============================================================================
 *
 * De Strategy Pattern definieert een familie van algoritmes, encapsuleert elk
 * algoritme, en maakt ze uitwisselbaar. Strategy laat het algoritme variëren
 * onafhankelijk van de clients die het gebruiken.
 *
 * In deze applicatie:
 * - ScoringStrategy is de Strategy interface
 * - StandardScoringStrategy is een ConcreteStrategy (normale scoring)
 * - StrictScoringStrategy is een ConcreteStrategy (strengere scoring)
 * - DrivingTrackerViewModel is de Context die de strategy gebruikt
 *
 * Voordelen:
 * - Makkelijk nieuwe scoring algoritmes toevoegen zonder bestaande code te wijzigen
 * - Runtime wisselen van algoritme mogelijk
 * - Open/Closed Principle: open voor uitbreiding, gesloten voor modificatie
 */
interface ScoringStrategy {
    /**
     * Berekent een lokale rijgedrag score op basis van acceleratie en remgedrag.
     *
     * @param maxAccel Maximale acceleratie in m/s²
     * @param maxBraking Maximale remkracht in m/s²
     * @param harshAccelCount Aantal harde acceleraties
     * @param harshBrakeCount Aantal harde rembewegingen
     * @return Score van 0-100 (100 = perfect rijgedrag)
     */
    fun calculateLocalScore(
        maxAccel: Double,
        maxBraking: Double,
        harshAccelCount: Int,
        harshBrakeCount: Int
    ): Int

    /**
     * Retourneert een beschrijving van de strategy
     */
    fun getDescription(): String
}