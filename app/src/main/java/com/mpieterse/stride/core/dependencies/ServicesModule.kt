package com.mpieterse.stride.core.dependencies

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.ConfigurationService
import com.mpieterse.stride.core.services.FcmTokenManager
import com.mpieterse.stride.core.services.LocalizationService
import com.mpieterse.stride.core.services.NotificationSchedulerService
import com.mpieterse.stride.data.remote.SummitApiService
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
    fun provideLocalizationService(
        application: Application
    ): LocalizationService = LocalizationService(
        application
    )


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

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext ctx: Context
    ): WorkManager = WorkManager.getInstance(ctx)

    @Provides
    @Singleton
    fun provideNotificationSchedulerService(
        @ApplicationContext ctx: Context,
        workManager: WorkManager
    ): NotificationSchedulerService = NotificationSchedulerService(ctx, workManager)

    @Provides
    @Singleton
    fun provideFcmTokenManager(
        @ApplicationContext ctx: Context,
        apiService: SummitApiService
    ): FcmTokenManager = FcmTokenManager(ctx, apiService)
}