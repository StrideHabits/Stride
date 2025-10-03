package com.mpieterse.stride.data.remote

import com.mpieterse.stride.data.dto.auth.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.dto.settings.*
import com.mpieterse.stride.data.dto.checkins.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

// TODO: need to double check against swagger
interface SummitApiService {
    // Auth
    @POST("api/users/register") suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>
    @POST("api/users/login")    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // Habits
    @GET("api/habits") suspend fun getHabits(): Response<List<HabitDto>>
    @POST("api/habits") suspend fun createHabit(@Body body: CreateHabitRequest): Response<HabitDto>
    @DELETE("api/habits/{id}") suspend fun deleteHabit(@Path("id") id: String): Response<Unit>

    // Check-ins
    @POST("api/checkins/{habitId}/complete")
    suspend fun completeHabit(@Path("habitId") id: String, @Query("date") isoDate: String): Response<CheckinDto>

    // Settings
    @GET("api/settings") suspend fun getSettings(): Response<SettingsDto>
    @PUT("api/settings") suspend fun updateSettings(@Body s: SettingsDto): Response<SettingsDto>

    // Uploads
    @Multipart @POST("api/uploads")
    suspend fun upload(@Part file: MultipartBody.Part): Response<Map<String, String>>
}
