package com.psspl.autoreply.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.psspl.autoreply.data.auth.AuthRepository
import com.psspl.autoreply.data.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Extension property — one DataStore per application process.
private val Context.authDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "auth_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /** Binds the concrete implementation to the [AuthRepository] interface. */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.authDataStore
    }
}
