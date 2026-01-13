package com.example.rentmycar_android_app.di

import com.example.rentmycar_android_app.network.*
import com.example.rentmycar_android_app.network.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @UnauthenticatedClient
    fun provideUnauthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @UnauthenticatedClient
    fun provideUnauthenticatedRetrofit(
        @UnauthenticatedClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedRetrofit(
        @AuthenticatedClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Service Providers
    @Provides
    @Singleton
    fun provideAuthService(
        @UnauthenticatedClient retrofit: Retrofit
    ): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideCarService(
        @AuthenticatedClient retrofit: Retrofit
    ): CarService {
        return retrofit.create(CarService::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentService(
        @AuthenticatedClient retrofit: Retrofit
    ): PaymentService {
        return retrofit.create(PaymentService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService(
        @AuthenticatedClient retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReservationService(
        @AuthenticatedClient retrofit: Retrofit
    ): ReservationService {
        return retrofit.create(ReservationService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocodingService(
        @UnauthenticatedClient retrofit: Retrofit
    ): GeocodingService {
        return retrofit.create(GeocodingService::class.java)
    }

    @Provides
    @Singleton
    fun provideRoutingService(
        @UnauthenticatedClient retrofit: Retrofit
    ): RoutingService {
        return retrofit.create(RoutingService::class.java)
    }
}
