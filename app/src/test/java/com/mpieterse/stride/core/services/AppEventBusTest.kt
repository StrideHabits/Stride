package com.mpieterse.stride.core.services

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AppEventBusTest {

    @Test
    fun `emit delivers events to collectors`() = runBlocking {
        val bus = AppEventBus()

        val job = launch {
            val event = bus.events.first()
            assertTrue(event is AppEventBus.AppEvent.HabitCreated)
        }

        bus.emit(AppEventBus.AppEvent.HabitCreated)
        job.join()
    }
}

