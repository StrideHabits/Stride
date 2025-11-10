package com.mpieterse.stride.core.dependencies

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mpieterse.stride.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore("stride_prefs")

private const val DB_NAME = "stride_dev_v1.db"   // ← human-readable, dev-only

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    @JvmStatic
    fun db(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

}

/*
    @Provides fun provideHabitDao(db: AppDatabase): HabitDao = db.habits()
    @Provides fun provideCheckInDao(db: AppDatabase): CheckInDao = db.checkIns()
    @Provides fun provideMutationDao(db: AppDatabase): MutationDao = db.mutations()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> = ctx.dataStore
}
*/