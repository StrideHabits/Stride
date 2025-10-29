package com.mpieterse.stride.data.remote

import com.mpieterse.stride.data.dto.auth.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.dto.checkins.*
import com.mpieterse.stride.data.dto.settings.*
import com.mpieterse.stride.data.dto.uploads.*
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
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse> //This method registers a new user through the REST API using Retrofit (GeeksforGeeks, 2017).

    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse> //This method authenticates a user through the REST API using Retrofit (GeeksforGeeks, 2017).

    // Habits
    @GET("api/habits")
    suspend fun getHabits(): Response<List<HabitDto>> //This method retrieves all habits from the REST API using Retrofit (GeeksforGeeks, 2017).

    @POST("api/habits")
    suspend fun createHabit(@Body body: HabitCreateDto): Response<HabitDto> //This method creates a new habit through the REST API using Retrofit (GeeksforGeeks, 2017).

    // CheckIns
    @GET("api/checkins")
    suspend fun getCheckIns(): Response<List<CheckInDto>> //This method retrieves all check-ins from the REST API using Retrofit (GeeksforGeeks, 2017).

    // Create a new check-in
    @POST("api/checkins")
    suspend fun createCheckIn(@Body body: CheckInCreateDto): Response<CheckInDto> //This method creates a new check-in through the REST API using Retrofit (GeeksforGeeks, 2017).


    // Settings
    @GET("api/settings")
    suspend fun getSettings(): Response<SettingsDto> //This method retrieves user settings from the REST API using Retrofit (GeeksforGeeks, 2017).

    @PUT("api/settings")
    suspend fun updateSettings(@Body body: SettingsDto): Response<SettingsDto> //This method updates user settings through the REST API using Retrofit (GeeksforGeeks, 2017).

    // Uploads
    @Multipart
    @POST("api/uploads")
    suspend fun upload(@Part file: MultipartBody.Part): Response<UploadResponse> //This method uploads files to the REST API using Retrofit multipart requests (GeeksforGeeks, 2017).


    @POST("sync/checkins/push")
    suspend fun syncPush(@Body items: List<com.mpieterse.stride.data.remote.models.PushItem>)
            : List<com.mpieterse.stride.data.remote.models.PushResult>

    @GET("sync/checkins/changes")
    suspend fun syncChanges(
        @Query("since") since: String?,
        @Query("pageSize") pageSize: Int = 200
    ): com.mpieterse.stride.data.remote.models.ChangesPage

}
