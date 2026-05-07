package com.psspl.autoreply.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Application-scoped dependencies will be provided here in later phases
}
