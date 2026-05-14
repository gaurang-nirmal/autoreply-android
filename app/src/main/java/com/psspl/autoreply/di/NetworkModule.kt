package com.psspl.autoreply.di

import com.psspl.autoreply.data.network.ApiService
import com.psspl.autoreply.data.remote.AuthApiService
import com.psspl.autoreply.data.remote.DriveApiService
import com.psspl.autoreply.data.remote.SheetsApiService
import com.psspl.autoreply.data.remote.interceptor.AuthInterceptor
import com.psspl.autoreply.data.remote.interceptor.CurlLoggingInterceptor
import com.psspl.autoreply.utils.AppConstants
import com.psspl.autoreplyclone.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("Plain")
    fun providePlainOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)   // attaches Bearer token when signed in
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(CurlLoggingInterceptor())
                }
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // ── Google APIs (Sheets + Drive) ──────────────────────────────────────────
    // Separate Retrofit instances are needed because the base URLs differ from
    // the app backend. The Authorization header is passed per-call (Bearer token)
    // so no dedicated interceptor is required here.

    @Provides
    @Singleton
    @Named("SheetsRetrofit")
    fun provideSheetsRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("DriveRetrofit")
    fun provideDriveRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSheetsApiService(@Named("SheetsRetrofit") retrofit: Retrofit): SheetsApiService =
        retrofit.create(SheetsApiService::class.java)

    @Provides
    @Singleton
    fun provideDriveApiService(@Named("DriveRetrofit") retrofit: Retrofit): DriveApiService =
        retrofit.create(DriveApiService::class.java)
}
