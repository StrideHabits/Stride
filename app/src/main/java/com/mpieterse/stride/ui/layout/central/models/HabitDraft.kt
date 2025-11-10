package com.mpieterse.stride.ui.layout.central.models

data class HabitDraft(
    val name: String,
    val frequency: Int = 0,
    val tag: String? = null,
    val imageBase64: String? = null,
    val imageMimeType: String? = null,
    val imageFileName: String? = null
)

