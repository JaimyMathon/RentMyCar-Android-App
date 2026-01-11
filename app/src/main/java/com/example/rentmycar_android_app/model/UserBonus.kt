package com.example.rentmycar_android_app.model

data class UserBonus(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val totalPoints: Int = 0,
    val lastUpdated: Long = 0L
)