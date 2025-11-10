package com.mpieterse.stride.data.remote

import android.content.Context
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.local.NotificationsStore
import com.google.gson.GsonBuilder
import com.mpieterse.stride.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Dependency Injection (DI) module for providing application-level dependencies.
 *
 * This module uses Hilt to manage and inject dependencies such as repositories,
 * services, and data sources throughout the app. Centralizing object creation
 * improves testability, scalability, and adherence to clean architecture principles.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection">
 *      Android Developers (2025). Dependency injection in Android.</a>
 *      [Accessed 6 Oct. 2025].
 */


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun tokenStore(@ApplicationContext c: Context) = TokenStore(c)
    @Provides @Singleton fun notificationsStore(@ApplicationContext c: Context) = NotificationsStore(c)

    @Provides @Singleton
    fun authInterceptor(store: TokenStore) = Interceptor { chain ->
        val token = runBlocking { store.tokenFlow.first() }
        val req = if (!token.isNullOrBlank())
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        else chain.request()
        chain.proceed(req)
    }

    @Provides @Singleton
    fun okHttp(auth: Interceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logger)
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(20))
            .writeTimeout(java.time.Duration.ofSeconds(20))
            .build()
    }

    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL) // must end with '/'
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(client)
            .build()

    @Provides @Singleton
    fun api(retrofit: Retrofit): SummitApiService =
        retrofit.create(SummitApiService::class.java)
}
