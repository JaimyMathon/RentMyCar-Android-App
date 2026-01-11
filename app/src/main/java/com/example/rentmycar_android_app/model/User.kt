package com.example.rentmycar_android_app.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String?  = null,
    val password:  String = ""
)

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val phone: String? = null
)