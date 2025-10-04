package com.mpieterse.stride.data.remote

import com.mpieterse.stride.data.dto.auth.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.dto.checkins.*
import com.mpieterse.stride.data.dto.settings.*
import com.mpieterse.stride.data.dto.uploads.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SummitApiService {

    // Users
    @POST("api/users/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // Habits
    @GET("api/Habits")
    suspend fun getHabits(): Response<List<HabitDto>>

    @POST("api/Habits")
    suspend fun createHabit(@Body body: HabitCreateDto): Response<HabitDto>

    // CheckIns
    @GET("api/CheckIns")
    suspend fun getCheckIns(): Response<List<CheckInDto>>

    // Create a new check-in
    @POST("api/CheckIns")
    suspend fun createCheckIn(@Body body: CheckInCreateDto): Response<CheckInDto>


    // Settings
    @GET("api/Settings")
    suspend fun getSettings(): Response<SettingsDto>

    @PUT("api/Settings")
    suspend fun updateSettings(@Body body: SettingsDto): Response<SettingsDto>

    // Uploads
    @Multipart
    @POST("api/Uploads")
    suspend fun upload(@Part file: MultipartBody.Part): Response<UploadResponse>
}
