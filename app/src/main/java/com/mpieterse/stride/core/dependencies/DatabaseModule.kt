// core/dependencies/DatabaseModule.kt
package com.mpieterse.stride.core.dependencies

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mpieterse.stride.BuildConfig
import com.mpieterse.stride.data.local.dao.CheckInDao
import com.mpieterse.stride.data.local.dao.HabitDao
import com.mpieterse.stride.data.local.dao.MutationDao
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "stride_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "stride.db")
            .addMigrations(MIGRATION_1_2)
            .apply {
                if (BuildConfig.DEBUG) {
                    // dev-only safety valves
                    fallbackToDestructiveMigrationOnDowngrade()
                }
            }
            .build()

    @Provides fun provideHabitDao(db: AppDatabase): HabitDao = db.habits()
    @Provides fun provideCheckInDao(db: AppDatabase): CheckInDao = db.checkIns()
    @Provides fun provideMutationDao(db: AppDatabase): MutationDao = db.mutations()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> = ctx.dataStore
}
