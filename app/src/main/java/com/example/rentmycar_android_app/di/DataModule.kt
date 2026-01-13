package com.example.rentmycar_android_app.di

import com.example.rentmycar_android_app.data.auth.TokenManager
import com.example.rentmycar_android_app.data.auth.TokenManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTokenManager(
        tokenManagerImpl: TokenManagerImpl
    ): TokenManager
}
