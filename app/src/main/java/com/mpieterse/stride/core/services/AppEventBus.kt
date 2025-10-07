package com.mpieterse.stride.core.services

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEventBus @Inject constructor() {
    
    private val _events = MutableSharedFlow<AppEvent>()
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()
    
    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
    
    sealed class AppEvent {
        object HabitCreated : AppEvent()
        object HabitUpdated : AppEvent()
        object CheckInCompleted : AppEvent()
        object HabitNameChanged : AppEvent()
    }
}
