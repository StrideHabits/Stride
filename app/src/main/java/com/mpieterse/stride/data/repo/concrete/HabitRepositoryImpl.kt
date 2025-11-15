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
import java.time.Instant
import java.util.UUID
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
            // 2) Cache locally as Synced - preserve imageUrl from input if API response doesn't include it
            val finalImageUrl = created.imageUrl?.takeIf { it.isNotBlank() } ?: input.imageUrl?.takeIf { it.isNotBlank() }
            
            Clogger.d("HabitRepository", "Creating habit ${created.id}: input imageUrl='${input.imageUrl}', API response imageUrl='${created.imageUrl}', final='$finalImageUrl'")
            
            db.withTransaction {
                db.habits().upsert(
                    HabitEntity(
                        id = created.id,
                        name = created.name,
                        frequency = created.frequency,
                        tag = created.tag,
                        imageUrl = finalImageUrl,
                        createdAt = created.createdAt,
                        updatedAt = created.updatedAt,
                        deleted = false,
                        rowVersion = "",           // fill if your API returns one
                        syncState = SyncState.Synced
                    )
                )
            }
            Clogger.d("HabitRepository", "Successfully created habit ${created.id} on server and cached locally${if (finalImageUrl != null) " with image $finalImageUrl" else " (no image)"}")
            return created.copy(imageUrl = finalImageUrl)
        } catch (e: HttpException) {
            // For server errors (500, 502, 503, 504), save locally with Pending sync state
            // This allows the habit to be visible immediately and synced later
            if (e.code() == 500 || e.code() == 502 || e.code() == 503 || e.code() == 504) {
                Clogger.w("HabitRepository", "Server error (${e.code()}) when creating habit, saving locally with Pending sync state")
                
                // Generate a local ID if server didn't return one
                val habitId = UUID.randomUUID().toString()
                val now = Instant.now().toString()
                val finalImageUrl = input.imageUrl?.takeIf { it.isNotBlank() }
                
                val localEntity = HabitEntity(
                    id = habitId,
                    name = input.name.trim(),
                    frequency = input.frequency.coerceIn(1, 7),
                    tag = input.tag?.trim().takeUnless { it.isNullOrEmpty() },
                    imageUrl = finalImageUrl,
                    createdAt = now,
                    updatedAt = now,
                    deleted = false,
                    rowVersion = "",
                    syncState = SyncState.Pending // Mark for future sync
                )
                
                db.withTransaction {
                    db.habits().upsert(localEntity)
                }
                
                Clogger.d("HabitRepository", "Saved habit $habitId locally with Pending sync state${if (finalImageUrl != null) " with image $finalImageUrl" else " (no image)"}")
                return localEntity.toDto()
            }
            // For other HTTP errors (400, 401, 403, 404), throw the exception
            Clogger.e("HabitRepository", "Failed to create habit: HTTP ${e.code()} ${e.message()}", e)
            throw e
        } catch (e: Exception) {
            // For network errors or other exceptions, also save locally if possible
            Clogger.e("HabitRepository", "Failed to create habit (network/exception), saving locally with Pending sync state", e)
            
            // Generate a local ID
            val habitId = UUID.randomUUID().toString()
            val now = Instant.now().toString()
            val finalImageUrl = input.imageUrl?.takeIf { it.isNotBlank() }
            
            val localEntity = HabitEntity(
                id = habitId,
                name = input.name.trim(),
                frequency = input.frequency.coerceIn(1, 7),
                tag = input.tag?.trim().takeUnless { it.isNullOrEmpty() },
                imageUrl = finalImageUrl,
                createdAt = now,
                updatedAt = now,
                deleted = false,
                rowVersion = "",
                syncState = SyncState.Pending // Mark for future sync
            )
            
            try {
                db.withTransaction {
                    db.habits().upsert(localEntity)
                }
                Clogger.d("HabitRepository", "Saved habit $habitId locally with Pending sync state after network/exception${if (finalImageUrl != null) " with image $finalImageUrl" else " (no image)"}")
                return localEntity.toDto()
            } catch (dbException: Exception) {
                // If we can't save to DB, throw the original exception
                Clogger.e("HabitRepository", "Failed to save habit locally, throwing original exception", dbException)
                throw e
            }
        }
    }

    // ---- Update (remote -> cache) ----

    override suspend fun update(id: String, input: HabitCreateDto): HabitDto {
        try {
            // Get existing habit to preserve imageUrl if needed
            val existingHabit = db.habits().getById(id)
            // 1) Call API
            val updated: HabitDto = api.updateHabit(id, input)
            // 2) Cache locally as Synced - preserve imageUrl from input or existing if API response doesn't include it
            val finalImageUrl = updated.imageUrl?.takeIf { it.isNotBlank() } 
                ?: input.imageUrl?.takeIf { it.isNotBlank() } 
                ?: existingHabit?.imageUrl?.takeIf { it.isNotBlank() }
            
            Clogger.d("HabitRepository", "Updating habit $id: input imageUrl='${input.imageUrl}', existing imageUrl='${existingHabit?.imageUrl}', API response imageUrl='${updated.imageUrl}', final='$finalImageUrl'")
            
            db.withTransaction {
                db.habits().upsert(
                    HabitEntity(
                        id = updated.id,
                        name = updated.name,
                        frequency = updated.frequency,
                        tag = updated.tag,
                        imageUrl = finalImageUrl,
                        createdAt = updated.createdAt,
                        updatedAt = updated.updatedAt,
                        deleted = false,
                        rowVersion = "",           // fill if your API returns one
                        syncState = SyncState.Synced
                    )
                )
            }
            Clogger.d("HabitRepository", "Successfully updated habit $id on server and cached locally${if (finalImageUrl != null) " with image $finalImageUrl" else " (no image)"}")
            return updated.copy(imageUrl = finalImageUrl)
        } catch (e: HttpException) {
            // If habit doesn't exist on server (404), try to create it instead
            if (e.code() == 404) {
                Clogger.w("HabitRepository", "Habit $id not found on server (404), attempting to create it instead")
                try {
                    // Try to create the habit instead
                    val created: HabitDto = api.createHabit(input)
                    // Use the ID returned by the server (might be different if server generates IDs)
                    // Preserve imageUrl from input if API response doesn't include it
                    val finalImageUrl = created.imageUrl?.takeIf { it.isNotBlank() } ?: input.imageUrl?.takeIf { it.isNotBlank() }
                    val habitToCache = created.copy(imageUrl = finalImageUrl)
                    db.withTransaction {
                        db.habits().upsert(
                            HabitEntity(
                                id = habitToCache.id,
                                name = habitToCache.name,
                                frequency = habitToCache.frequency,
                                tag = habitToCache.tag,
                                imageUrl = habitToCache.imageUrl,
                                createdAt = habitToCache.createdAt,
                                updatedAt = habitToCache.updatedAt,
                                deleted = false,
                                rowVersion = "",
                                syncState = SyncState.Synced
                            )
                        )
                        // If server generated a new ID, update the old habit to be deleted locally
                        if (habitToCache.id != id) {
                            db.habits().getById(id)?.let { oldHabit ->
                                db.habits().upsert(oldHabit.copy(deleted = true))
                            }
                        }
                    }
                    Clogger.d("HabitRepository", "Successfully created habit ${habitToCache.id} on server (after 404 on update for $id) and cached locally${if (finalImageUrl != null) " with image" else ""}")
                    return habitToCache
                } catch (createException: HttpException) {
                    // If create fails with 400 (already exists) or 500 (server error), 
                    // update locally with current data and mark as needing sync
                    if (createException.code() == 400 || createException.code() == 500) {
                        Clogger.w("HabitRepository", "Create failed after 404 (${createException.code()}), updating local cache and marking for sync")
                        val localHabit = db.habits().getById(id)
                        if (localHabit != null) {
                            val updatedAt = Instant.now().toString()
                            // Preserve imageUrl from input, or fall back to existing local imageUrl
                            val preservedImageUrl = input.imageUrl?.takeIf { it.isNotBlank() } 
                                ?: localHabit.imageUrl?.takeIf { it.isNotBlank() }
                            val updatedEntity = HabitEntity(
                                id = localHabit.id,
                                name = input.name,
                                frequency = input.frequency,
                                tag = input.tag,
                                imageUrl = preservedImageUrl,
                                createdAt = localHabit.createdAt,
                                updatedAt = updatedAt,
                                deleted = false,
                                rowVersion = localHabit.rowVersion,
                                syncState = SyncState.Pending // Mark for future sync
                            )
                            db.withTransaction {
                                db.habits().upsert(updatedEntity)
                            }
                            Clogger.d("HabitRepository", "Updated habit $id locally after create failure, marked for sync")
                            // Return a DTO from the updated entity
                            return updatedEntity.toDto()
                        }
                    }
                    Clogger.e("HabitRepository", "Failed to create habit after 404 on update", createException)
                    throw e // Throw original 404 exception if create also fails and we can't recover
                } catch (createException: Exception) {
                    Clogger.e("HabitRepository", "Failed to create habit after 404 on update", createException)
                    throw e // Throw original 404 exception if create also fails
                }
            } else {
                Clogger.e("HabitRepository", "Failed to update habit: HTTP ${e.code()} ${e.message()}", e)
                throw e
            }
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
                // Get existing habits to preserve imageUrl when remote doesn't have it
                val existingHabits = db.habits().list().associateBy { it.id }
                
                // Merge remote data with local, preserving imageUrl from local if remote doesn't have it
                val habitsToSave = remote.map { remoteDto ->
                    val existing = existingHabits[remoteDto.id]
                    // Preserve local imageUrl if remote doesn't have a valid (non-blank) one
                    // Only use remote imageUrl if it's not null and not blank
                    val finalImageUrl = when {
                        // Remote has a valid imageUrl - use it
                        !remoteDto.imageUrl.isNullOrBlank() -> remoteDto.imageUrl
                        // Remote doesn't have one, but local does - preserve local
                        existing?.imageUrl?.isNotBlank() == true -> existing.imageUrl
                        // Neither has one - use null
                        else -> null
                    }
                    
                    Clogger.d("HabitRepository", "Merging habit ${remoteDto.id}: remote imageUrl='${remoteDto.imageUrl}', local imageUrl='${existing?.imageUrl}', final='$finalImageUrl'")
                    
                    val mergedDto = remoteDto.copy(imageUrl = finalImageUrl)
                    mergedDto.toEntity(syncState = SyncState.Synced)
                }
                
                // Replace all habits with merged data
                db.habits().replaceAll(habitsToSave)
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
