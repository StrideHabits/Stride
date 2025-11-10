// data/dto/settings/SettingsDto.kt
package com.mpieterse.stride.data.dto.settings

data class SettingsDto(
    val dailyReminderHour: Int?,   // nullable by design
    val theme: String?             // nullable by design
)
