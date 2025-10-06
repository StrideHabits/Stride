package com.mpieterse.stride.core.dependencies

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.ConfigurationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {

    @Provides
    @Singleton
    fun provideConfigurationService(
        @ApplicationContext ctx: Context
    ): ConfigurationService = ConfigurationService(ctx)


    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext ctx: Context
    ): CredentialManager = CredentialManager.create(ctx)


    @Provides
    @Singleton
    fun provideAuthService(
        firebaseAuth: FirebaseAuth
    ): AuthenticationService = AuthenticationService(firebaseAuth)
}