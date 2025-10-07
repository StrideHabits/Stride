package com.mpieterse.stride.core.services

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitNameOverrideService @Inject constructor() {
    
    private val nameOverrides = mutableMapOf<String, String>()
    
    fun updateHabitName(habitId: String, newName: String) {
        nameOverrides[habitId] = newName
    }
    
    fun getDisplayName(habitId: String, originalName: String): String {
        return nameOverrides[habitId] ?: originalName
    }
    
    fun clearOverride(habitId: String) {
        nameOverrides.remove(habitId)
    }
}
