// app/src/main/java/com/mpieterse/stride/core/dependencies/RepositoryModule.kt
package com.mpieterse.stride.core.dependencies

import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.repo.concrete.CheckInRepositoryImpl
import com.mpieterse.stride.data.repo.concrete.HabitRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindHabitRepo(impl: HabitRepositoryImpl): HabitRepository
    @Binds @Singleton abstract fun bindCheckInRepo(impl: CheckInRepositoryImpl): CheckInRepository
}
