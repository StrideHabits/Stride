// data/local/db/Converters.kt
package com.mpieterse.stride.data.local.db

import androidx.room.TypeConverter
import com.mpieterse.stride.data.local.entities.SyncState

class Converters {
    @TypeConverter fun fromSyncState(s: SyncState): String = s.name
    @TypeConverter fun toSyncState(v: String): SyncState = SyncState.valueOf(v)
}
