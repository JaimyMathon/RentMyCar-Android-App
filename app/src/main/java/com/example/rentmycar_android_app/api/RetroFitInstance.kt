package com.example.rentmycar_android_app.api

import android.annotation.SuppressLint
import android.content.Context
import com.example.rentmycar_android_app.network.ApiClient
import com.example.rentmycar_android_app.network.ApiClientWithToken
import retrofit2.Retrofit

object RetrofitInstance {

    @SuppressLint("StaticFieldLeak")
    private lateinit var retrofit: Retrofit

    /**
     * Call this once from your Application or MainActivity:
     * RetrofitInstance.init(applicationContext)
     */
    fun init(context: Context) {
        retrofit = ApiClient.instance
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
