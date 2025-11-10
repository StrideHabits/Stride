// data/local/db/Converters.kt
package com.mpieterse.stride.data.local.db

import androidx.room.TypeConverter
import com.mpieterse.stride.data.local.entities.*

class Converters {
    @TypeConverter fun fromSyncState(v: SyncState) = v.name
    @TypeConverter fun toSyncState(v: String) = enumValueOf<SyncState>(v)

    @TypeConverter fun fromTargetType(v: TargetType) = v.name
    @TypeConverter fun toTargetType(v: String) = enumValueOf<TargetType>(v)

    @TypeConverter fun fromMutationOp(v: MutationOp) = v.name
    @TypeConverter fun toMutationOp(v: String) = enumValueOf<MutationOp>(v)

    @TypeConverter fun fromMutationState(v: MutationState) = v.name
    @TypeConverter fun toMutationState(v: String) = enumValueOf<MutationState>(v)
}
