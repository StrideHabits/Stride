package com.mpieterse.stride.data.remote

import com.mpieterse.stride.data.dto.auth.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.dto.checkins.*
import com.mpieterse.stride.data.dto.settings.*
import com.mpieterse.stride.data.dto.uploads.*
import com.mpieterse.stride.data.remote.models.ChangesPage
import com.mpieterse.stride.data.remote.models.PushItem
import com.mpieterse.stride.data.remote.models.PushResult
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SummitApiService {

    // Users
    @POST("api/users/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // Habits (CRUD via /api)
    // FIX: All habit CRUD operations now use the "api/" prefix for consistency.

    @GET("api/habits") // FIXED: Added "api/"
    suspend fun listHabits(): List<HabitDto>

    @POST("api/habits") // FIXED: Added "api/" <-- This is the 404 fix
    suspend fun createHabit(@Body body: HabitCreateDto): HabitDto

    @DELETE("api/habits/{id}") // FIXED: Added "api/"
    suspend fun deleteHabit(@Path("id") id: String)

    @GET("api/habits") // This one was already correct
    suspend fun getHabits(): Response<List<HabitDto>>

    // Check-ins (CRUD via /api)
    @GET("api/checkins")
    suspend fun getCheckIns(
        @Query("sinceUtc") sinceUtc: String? = null
    ): Response<List<CheckInDto>>

    @POST("api/checkins")
    suspend fun createCheckIn(@Body body: CheckInCreateDto): Response<CheckInDto>

    // Settings
    @GET("api/settings")
    suspend fun getSettings(): Response<SettingsDto>

    @PUT("api/settings")
    suspend fun updateSettings(@Body body: SettingsDto): Response<SettingsDto>

    // Uploads
    @Multipart
    @POST("api/uploads")
    suspend fun upload(@Part file: MultipartBody.Part): Response<UploadResponse>

    // Sync (CHECK-INS ONLY) via /sync (no api/ prefix)
    @POST("sync/checkins/push")
    suspend fun syncPush(@Body items: List<PushItem>): List<PushResult>

    @GET("sync/checkins/changes")
    suspend fun syncChanges(
        @Query("since") since: String?,
        @Query("pageSize") pageSize: Int = 200
    ): ChangesPage
}