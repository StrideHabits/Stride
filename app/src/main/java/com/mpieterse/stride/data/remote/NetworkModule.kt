package com.mpieterse.stride.data.remote

import android.content.Context
import com.mpieterse.stride.data.local.TokenStore
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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun tokenStore(@ApplicationContext c: Context) = TokenStore(c)

    @Provides @Singleton
    fun authInterceptor(store: TokenStore) = Interceptor { chain ->
        val token = runBlocking { store.tokenFlow.first() }
        val req = if (!token.isNullOrBlank())
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        else chain.request()
        chain.proceed(req)
    }

    @Provides @Singleton
    fun okHttp(auth: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(client)
            .build()

    @Provides @Singleton
    fun api(retrofit: Retrofit): SummitApiService = retrofit.create(SummitApiService::class.java)
}
