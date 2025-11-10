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

/**
 * Service interface for communicating with the SummitAPI backend using Retrofit.
 *
 * This class defines the HTTP endpoints and request/response models used to interact
 * with the remote RESTful API. Retrofit handles serialization, request execution,
 * and network layer abstraction, enabling a clean separation between network logic
 * and application layers.
 *
 * @see <a href="https://www.geeksforgeeks.org/android/introduction-retofit-2-android-set-1/">
 *      GeeksforGeeks (2017). Introduction to Retrofit in Android.</a>
 *      [Accessed 6 Oct. 2025].
 */


interface SummitApiService {

    // Users
    @POST("api/users/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // Habits (CRUD via /api)
    @GET("api/habits")
    suspend fun getHabits(): Response<List<HabitDto>>

    @POST("api/habits")
    suspend fun createHabit(@Body body: HabitCreateDto): Response<HabitDto>

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
    suspend fun upload(@Part file: MultipartBody.Part): Response<UploadResponse> //This method uploads files to the REST API using Retrofit multipart requests (GeeksforGeeks, 2017).

    // Notifications / FCM
    @POST("api/notifications/fcm-token")
    suspend fun registerFcmToken(@Body body: FcmTokenRequest): Response<FcmTokenResponse> //This method registers FCM token with the backend through the REST API using Retrofit (GeeksforGeeks, 2017).
}
