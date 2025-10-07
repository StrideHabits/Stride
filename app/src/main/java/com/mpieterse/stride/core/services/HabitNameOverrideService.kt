package com.mpieterse.stride.core.services

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitNameOverrideService @Inject constructor() {
    
    private val nameOverrides = mutableMapOf<String, String>()
    
    fun updateHabitName(habitId: String, newName: String) { //This method updates habit display names using in-memory storage (Android Developers, 2024).
        nameOverrides[habitId] = newName
    }
    
    fun getDisplayName(habitId: String, originalName: String): String { //This method retrieves habit display names from in-memory storage (Android Developers, 2024).
        return nameOverrides[habitId] ?: originalName
    }
    
    fun clearOverride(habitId: String) { //This method clears habit name overrides from in-memory storage (Android Developers, 2024).
        nameOverrides.remove(habitId)
    }
}
