// app/src/main/java/com/mpieterse/stride/data/repo/concrete/HabitRepositoryImpl.kt
package com.mpieterse.stride.data.repo.concrete

import android.content.Context
import androidx.room.withTransaction
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.dto.habits.HabitCreateDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.HabitEntity
import com.mpieterse.stride.data.local.entities.SyncState
import com.mpieterse.stride.data.mappers.toDto
import com.mpieterse.stride.data.mappers.toEntity
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.remote.SummitApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val api: SummitApiService,
    private val db: AppDatabase,
    @ApplicationContext private val appContext: Context,
    private val eventBus: com.mpieterse.stride.core.services.AppEventBus
) : HabitRepository {

    // ---- Observe / Get (local first) ----

    override fun observeAll(): Flow<List<HabitDto>> =
        db.habits().observeAll().map { list -> list.map { it.toDto() } }

    override fun observeById(id: String): Flow<HabitDto?> =
        db.habits().observeById(id).map { it?.toDto() }

    override suspend fun getAll(forceRemote: Boolean): List<HabitDto> {
        if (forceRemote) {
            // Try to pull from remote, but continue even if it fails
            pull()
        }
        return db.habits().list().map { it.toDto() }
    }

    override suspend fun getById(id: String, forceRemote: Boolean): HabitDto? {
        if (forceRemote) {
            // Try to pull from remote, but continue even if it fails
            pull()
        }
        return db.habits().getById(id)?.toDto()
    }

    // ---- Create (remote -> cache) ----

    override suspend fun create(input: HabitCreateDto): HabitDto {
        try {
            // 1) Call API
            val created: HabitDto = api.createHabit(input)
            // 2) Cache locally as Synced
            db.withTransaction {
                db.habits().upsert(
                    HabitEntity(
                        id = created.id,
                        name = created.name,
                        frequency = created.frequency,
                        tag = created.tag,
                        imageUrl = created.imageUrl,
                        createdAt = created.createdAt,
                        updatedAt = created.updatedAt,
                        deleted = false,
                        rowVersion = "",           // fill if your API returns one
                        syncState = SyncState.Synced
                    )
                )
            }
            return created
        } catch (e: HttpException) {
            Clogger.e("HabitRepository", "Failed to create habit: HTTP ${e.code()} ${e.message()}", e)
            throw e
        } catch (e: Exception) {
            Clogger.e("HabitRepository", "Failed to create habit", e)
            throw e
        }
    }

    // ---- Update (remote -> cache) ----

    override suspend fun update(id: String, input: HabitCreateDto): HabitDto {
        try {
            // 1) Call API
            val updated: HabitDto = api.updateHabit(id, input)
            // 2) Cache locally as Synced
            db.withTransaction {
                db.habits().upsert(
                    HabitEntity(
                        id = updated.id,
                        name = updated.name,
                        frequency = updated.frequency,
                        tag = updated.tag,
                        imageUrl = updated.imageUrl,
                        createdAt = updated.createdAt,
                        updatedAt = updated.updatedAt,
                        deleted = false,
                        rowVersion = "",           // fill if your API returns one
                        syncState = SyncState.Synced
                    )
                )
            }
            Clogger.d("HabitRepository", "Successfully updated habit $id on server and cached locally")
            return updated
        } catch (e: HttpException) {
            Clogger.e("HabitRepository", "Failed to update habit: HTTP ${e.code()} ${e.message()}", e)
            throw e
        } catch (e: Exception) {
            Clogger.e("HabitRepository", "Failed to update habit", e)
            throw e
        }
    }

    // ---- Local upsert helper ----

    override suspend fun upsertLocal(dto: HabitDto) {
        db.habits().upsert(dto.toEntity(syncState = SyncState.Synced))
    }

    // ---- Delete (remote -> cache) ----

    override suspend fun delete(id: String) {
        try {
            // remote delete then mark local
            api.deleteHabit(id)
            db.habits().markDeleted(id = id)
            // Emit event for notification cleanup
            eventBus.emit(com.mpieterse.stride.core.services.AppEventBus.AppEvent.HabitDeleted(id))
        } catch (e: HttpException) {
            Clogger.e("HabitRepository", "Failed to delete habit: HTTP ${e.code()} ${e.message()}", e)
            // Still mark as deleted locally even if API call fails
            db.habits().markDeleted(id = id)
            eventBus.emit(com.mpieterse.stride.core.services.AppEventBus.AppEvent.HabitDeleted(id))
            throw e
        } catch (e: Exception) {
            Clogger.e("HabitRepository", "Failed to delete habit", e)
            // Still mark as deleted locally even if API call fails
            db.habits().markDeleted(id = id)
            eventBus.emit(com.mpieterse.stride.core.services.AppEventBus.AppEvent.HabitDeleted(id))
            throw e
        }
    }

    override suspend fun clearLocal() {
        db.habits().clearAll()
    }

    // ---- Sync helpers ----
    // If you are doing remote-create, there is nothing to push for habits.
    override suspend fun pushHabitCreates(): Boolean = false

    override suspend fun pull(): Boolean {
        return try {
            val remote = api.listHabits() // returns List<HabitDto>
            db.withTransaction {
                // simple merge: replace snapshot
                db.habits().replaceAll(remote.map { it.toEntity(syncState = SyncState.Synced) })
            }
            Clogger.d("HabitRepository", "Successfully pulled ${remote.size} habits from server")
            true
        } catch (e: HttpException) {
            // Handle HTTP errors (400, 500, etc.)
            Clogger.e("HabitRepository", "Failed to pull habits: HTTP ${e.code()} ${e.message()}", e)
            // Return false to indicate failure, but don't throw - app can continue with cached data
            false
        } catch (e: Exception) {
            // Handle network errors, timeouts, etc.
            Clogger.e("HabitRepository", "Failed to pull habits", e)
            // Return false to indicate failure, but don't throw - app can continue with cached data
            false
        }
    }
}
